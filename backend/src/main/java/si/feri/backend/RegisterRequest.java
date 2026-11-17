
package si.feri.backend;

public record RegisterRequest(
        String username,
        String email,
        String password
) {
}
