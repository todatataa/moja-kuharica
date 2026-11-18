package si.feri.backend;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class RecipeRatingTests {
    @Autowired private RecipeController recipes;
    @Autowired private RecipeRepository recipeRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private EntityManager entityManager;

    @Test
    void preservesFirstScoreAndReturnsItInRecipeSummaries() {
        String suffix = UUID.randomUUID().toString();
        AppUser user = new AppUser();
        user.setUsername("rating-" + suffix);
        user.setEmail(user.getUsername() + "@example.com");
        user.setPasswordHash("unused");
        userRepository.save(user);
        var authentication = UsernamePasswordAuthenticationToken.unauthenticated(user.getUsername(), "");
        Recipe recipe = new Recipe();
        recipe.setTitle("Ocena " + suffix);
        Long id = recipeRepository.save(recipe).getId();
        JsonMapper json = new JsonMapper();

        Recipe rated = recipes.rate(id, json.createObjectNode().put("value", 4), authentication);
        assertEquals(new BigDecimal("4.0"), rated.getAverageRating());
        assertEquals(1, rated.getRatingCount());

        for (int invalid : new int[]{0, 6}) {
            var failure = assertThrows(ResponseStatusException.class,
                    () -> recipes.rate(id, json.createObjectNode().put("value", invalid), authentication));
            assertEquals(HttpStatus.BAD_REQUEST, failure.getStatusCode());
        }

        var duplicate = assertThrows(ResponseStatusException.class,
                () -> recipes.rate(id, json.createObjectNode().put("value", 5), authentication));
        assertEquals(HttpStatus.CONFLICT, duplicate.getStatusCode());

        entityManager.clear();
        Recipe listed = recipes.getAll(suffix, 0, 5, "id", "desc").content().getFirst();
        assertEquals(new BigDecimal("4.0"), listed.getAverageRating());
        assertEquals(1, listed.getRatingCount());
    }
}
