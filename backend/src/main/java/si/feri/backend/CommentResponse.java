package si.feri.backend;

import java.time.Instant;

public record CommentResponse(Long id, String text, String author, Instant createdAt) {}
