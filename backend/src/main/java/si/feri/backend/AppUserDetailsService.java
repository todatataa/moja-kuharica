package si.feri.backend;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class AppUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public AppUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Prijava je mogoča z uporabniškim imenom ali z e-naslovom (oba sta shranjena z malimi črkami).
    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        String value = identifier.trim().toLowerCase(Locale.ROOT);

        AppUser user = userRepository.findByUsername(value)
                .or(() -> userRepository.findByEmail(value))
                .orElseThrow(() -> new UsernameNotFoundException("Uporabnik ne obstaja."));

        return User.withUsername(user.getUsername())
                .password(user.getPasswordHash())
                .roles("USER")
                .build();
    }
}
