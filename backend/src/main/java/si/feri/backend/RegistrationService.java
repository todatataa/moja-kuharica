
package si.feri.backend;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

@Service
public class RegistrationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public RegistrationService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Long register(RegisterRequest request) {

        // Preverimo, ali so podatki prisotni.
        if (request == null
                || request.username() == null
                || request.email() == null
                || request.password() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Vsa polja so obvezna."
            );
        }

        String username = request.username().trim()
                .toLowerCase(Locale.ROOT);
        String email = request.email().trim()
                .toLowerCase(Locale.ROOT);
        String password = request.password();

        // Preverjanje uporabniskega imena
        if (username.length() < 3 || username.length() > 50) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Uporabnisko ime mora imeti od 3 do 50 znakov."
            );
        }

        // Preverjanje elektronskega naslova
        if (email.length() > 254
                || !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Elektronski naslov ni veljaven."
            );
        }

        // Preverjanje gesla
        if (password.length() < 8) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Geslo mora imeti vsaj 8 znakov."
            );
        }

        // Preverimo, ali uporabnik ze obstaja.
        if (userRepository.existsByUsername(username)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Uporabnisko ime je ze zasedeno."
            );
        }

        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Elektronski naslov je ze registriran."
            );
        }

        // Ustvarimo novega uporabnika.
        AppUser user = new AppUser();
        user.setUsername(username);
        user.setEmail(email);

        // Shranimo samo hash gesla, nikoli navadnega gesla.
        user.setPasswordHash(passwordEncoder.encode(password));

        AppUser savedUser = userRepository.save(user);

        // Vrnemo samo ID uporabnika, ne gesla ali hasha.
        return savedUser.getId();
    }
}
