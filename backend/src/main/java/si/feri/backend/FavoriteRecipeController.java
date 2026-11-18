
package si.feri.backend;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
public class FavoriteRecipeController {

    private final FavoriteRecipeService favoriteService;
    private final UserRepository userRepository;

    public FavoriteRecipeController(
            FavoriteRecipeService favoriteService,
            UserRepository userRepository) {

        this.favoriteService = favoriteService;
        this.userRepository = userRepository;
    }

    // Dodajanje recepta med priljubljene
    @PutMapping("/api/recipes/{id}/favorite")
    public ResponseEntity<Void> addFavorite(
            @PathVariable Long id,
            Authentication authentication) {

        AppUser user = getCurrentUser(authentication);
        favoriteService.addFavorite(user, id);

        return ResponseEntity.noContent().build();
    }

    // Odstranitev recepta iz priljubljenih
    @DeleteMapping("/api/recipes/{id}/favorite")
    public ResponseEntity<Void> removeFavorite(
            @PathVariable Long id,
            Authentication authentication) {

        AppUser user = getCurrentUser(authentication);
        favoriteService.removeFavorite(user, id);

        return ResponseEntity.noContent().build();
    }

    // Pridobivanje priljubljenih receptov
    @GetMapping("/api/me/favorites")
    public List<Recipe> getFavorites(
            Authentication authentication) {

        AppUser user = getCurrentUser(authentication);
        return favoriteService.getFavoriteRecipes(user);
    }

    // Pridobivanje trenutno prijavljenega uporabnika
    private AppUser getCurrentUser(Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Uporabnik ni prijavljen."
            );
        }

        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Uporabnik ne obstaja."
                ));
    }
}
