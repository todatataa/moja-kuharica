package si.feri.backend;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class CommentService {

    private static final int MAX_COMMENT_LENGTH = 500;
    private static final Pattern WHITESPACE = Pattern.compile("\\s*", Pattern.UNICODE_CHARACTER_CLASS);

    private final CommentRepository comments;
    private final RecipeRepository recipes;
    private final UserRepository users;

    public CommentService(CommentRepository comments, RecipeRepository recipes, UserRepository users) {
        this.comments = comments;
        this.recipes = recipes;
        this.users = users;
    }

    @Transactional
    public CommentResponse add(Long recipeId, String username, String text) {
        if (text == null || WHITESPACE.matcher(text).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vnesi besedilo komentarja.");
        }
        if (text.codePointCount(0, text.length()) > MAX_COMMENT_LENGTH) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Komentar lahko vsebuje največ 500 znakov.");
        }

        AppUser author = users.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Uporabnik ne obstaja več."));
        Recipe recipe = recipes.findById(recipeId).orElseThrow(this::recipeNotFound);
        Instant createdAt = Instant.now().truncatedTo(ChronoUnit.MILLIS);
        return toResponse(comments.save(new Comment(text, createdAt, recipe, author)));
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> list(Long recipeId) {
        if (!recipes.existsById(recipeId)) {
            throw recipeNotFound();
        }
        return comments.findAllByRecipe_IdOrderByCreatedAtDescIdDesc(recipeId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private CommentResponse toResponse(Comment comment) {
        return new CommentResponse(
                comment.getId(), comment.getText(), comment.getAuthor().getUsername(), comment.getCreatedAt());
    }

    private ResponseStatusException recipeNotFound() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Recept ne obstaja.");
    }
}
