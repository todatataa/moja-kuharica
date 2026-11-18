package si.feri.backend;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class RecipeCommentTests {

    @Autowired private MockMvc mvc;
    @Autowired private RecipeRepository recipes;
    @Autowired private CommentRepository comments;
    @Autowired private UserRepository users;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private EntityManager entityManager;

    private final JsonMapper json = new JsonMapper();
    private String username;
    private String recipeUrl;
    private String commentsUrl;

    @BeforeEach
    void setUp() {
        username = "comment-" + UUID.randomUUID();
        AppUser author = new AppUser();
        author.setUsername(username);
        author.setEmail(username + "@example.com");
        author.setPasswordHash(passwordEncoder.encode("test-password"));
        users.save(author);

        Recipe recipe = new Recipe();
        recipe.setTitle("Testni recept");
        recipe.setOwnerId(author.getId());
        recipeUrl = "/api/recipes/" + recipes.save(recipe).getId();
        commentsUrl = recipeUrl + "/comments";
    }

    @Test
    void publishesWithSessionAuthorAndListsNewestFirstForGuests() throws Exception {
        MockHttpSession session = login();
        String[] texts = {"Prvi komentar", "Drugi komentar", "Tretji komentar"};
        for (String text : texts) {
            mvc.perform(post(commentsUrl).session(session).contentType(MediaType.APPLICATION_JSON)
                            .content(json.createObjectNode().put("text", text).put("author", "drug-uporabnik")
                                    .put("createdAt", "2000-01-01T00:00:00Z").toString()))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").isNumber())
                    .andExpect(jsonPath("$.text").value(text))
                    .andExpect(jsonPath("$.author").value(username))
                    .andExpect(jsonPath("$.createdAt").isString())
                    .andExpect(jsonPath("$.passwordHash").doesNotExist());
        }

        mvc.perform(get(commentsUrl))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].text").value(texts[2]))
                .andExpect(jsonPath("$[0].author").value(username))
                .andExpect(jsonPath("$[0].createdAt").isString())
                .andExpect(jsonPath("$[1].text").value(texts[1]))
                .andExpect(jsonPath("$[2].text").value(texts[0]));
    }

    @Test
    void rejectsBlankAndOversizedCommentsAndAcceptsExactly500Characters() throws Exception {
        MockHttpSession session = login();
        for (String text : new String[]{"", " \t\n\u00a0", "a".repeat(501)}) {
            mvc.perform(post(commentsUrl).session(session).contentType(MediaType.APPLICATION_JSON)
                            .content(json.createObjectNode().put("text", text).toString()))
                    .andExpect(status().isBadRequest());
        }

        mvc.perform(get(commentsUrl))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        String maxLength = "a".repeat(500);
        mvc.perform(post(commentsUrl).session(session).contentType(MediaType.APPLICATION_JSON)
                        .content(json.createObjectNode().put("text", maxLength).toString()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.text").value(maxLength));
    }

    @Test
    void guestCannotPublish() throws Exception {
        mvc.perform(post(commentsUrl).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"Gostov komentar\"}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(get(commentsUrl))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void deletingRecipeAlsoDeletesItsComments() throws Exception {
        MockHttpSession session = login();
        String response = mvc.perform(post(commentsUrl).session(session).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"Komentar za izbris\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long commentId = json.readTree(response).get("id").asLong();

        mvc.perform(delete(recipeUrl).session(session))
                .andExpect(status().isNoContent());
        entityManager.flush();
        assertFalse(comments.existsById(commentId));

        mvc.perform(get(commentsUrl)).andExpect(status().isNotFound());
        mvc.perform(post(commentsUrl).session(session).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"Nov komentar\"}"))
                .andExpect(status().isNotFound());
    }

    private MockHttpSession login() throws Exception {
        return (MockHttpSession) mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(json.createObjectNode().put("username", username)
                                .put("password", "test-password").toString()))
                .andExpect(status().isOk())
                .andReturn().getRequest().getSession(false);
    }
}
