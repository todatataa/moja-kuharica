package si.feri.backend;

public record LoginRequest(
        String username,
        String password
) {
}
