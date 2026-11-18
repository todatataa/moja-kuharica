package si.feri.backend;

// Podatki o prijavljenem uporabniku. Gesla in hasha nikoli ne vračamo.
public record CurrentUserResponse(
        Long id,
        String username,
        String email
) {
}
