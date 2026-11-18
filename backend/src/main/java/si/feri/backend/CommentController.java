package si.feri.backend;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.JsonNode;

import java.util.List;

@RestController
@RequestMapping("/api/recipes/{id}/comments")
public class CommentController {

    private final CommentService comments;

    public CommentController(CommentService comments) {
        this.comments = comments;
    }

    @Operation(summary = "Vrne komentarje recepta, najnovejše najprej")
    @GetMapping
    public List<CommentResponse> list(@PathVariable Long id) {
        return comments.list(id);
    }

    @Operation(summary = "Doda komentar k receptu")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommentResponse add(@PathVariable Long id, @RequestBody(required = false) JsonNode body,
                               Authentication authentication) {
        JsonNode text = body == null ? null : body.get("text");
        return comments.add(id, authentication.getName(), text != null && text.isString() ? text.asString() : null);
    }
}
