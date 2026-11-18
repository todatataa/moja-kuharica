package si.feri.backend;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;
    private final UserRepository userRepository;

    private final SecurityContextHolderStrategy securityContextHolderStrategy =
            SecurityContextHolder.getContextHolderStrategy();
    private final SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();

    public AuthController(AuthenticationManager authenticationManager,
                          SecurityContextRepository securityContextRepository,
                          UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
        this.userRepository = userRepository;
    }

    @Operation(summary = "Prijava (uporabniško ime ali e-naslov in geslo), odpre sejo")
    @PostMapping("/login")
    public CurrentUserResponse login(@RequestBody LoginRequest body,
                                     HttpServletRequest request,
                                     HttpServletResponse response) {

        if (body == null || body.username() == null || body.username().isBlank()
                || body.password() == null || body.password().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Vnesi uporabniško ime in geslo.");
        }

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(
                            body.username().trim(), body.password()));
        } catch (AuthenticationException e) {
            // Namenoma ista napaka za neznanega uporabnika in napačno geslo.
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Napačno uporabniško ime ali geslo.");
        }

        // Zaščita pred session fixation: ob prijavi zamenjamo ID obstoječe seje.
        if (request.getSession(false) != null) {
            request.changeSessionId();
        }

        // Od Spring Security 6 je treba kontekst izrecno shraniti v sejo.
        SecurityContext context = securityContextHolderStrategy.createEmptyContext();
        context.setAuthentication(authentication);
        securityContextHolderStrategy.setContext(context);
        securityContextRepository.saveContext(context, request, response);

        return findCurrentUser(authentication.getName());
    }

    @Operation(summary = "Odjava, razveljavi sejo")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(Authentication authentication,
                                       HttpServletRequest request,
                                       HttpServletResponse response) {
        logoutHandler.logout(request, response, authentication);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Vrne trenutno prijavljenega uporabnika (401, če nihče ni prijavljen)")
    @GetMapping("/me")
    public CurrentUserResponse me(Authentication authentication) {
        return findCurrentUser(authentication.getName());
    }

    private CurrentUserResponse findCurrentUser(String username) {
        AppUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Uporabnik ne obstaja več."));
        return new CurrentUserResponse(user.getId(), user.getUsername(), user.getEmail());
    }
}
