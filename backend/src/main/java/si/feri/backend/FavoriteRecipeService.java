
package si.feri.backend;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class FavoriteRecipeService {

    private final FavoriteRecipeRepository favoriteRepository;
    private final RecipeRepository recipeRepository;

    public FavoriteRecipeService(
            FavoriteRecipeRepository favoriteRepository,
            RecipeRepository recipeRepository) {
        this.favoriteRepository = favoriteRepository;
        this.recipeRepository = recipeRepository;
    }

    // Dodajanje recepta med priljubljene
    @Transactional
    public void addFavorite(AppUser user, Long recipeId) {

        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Recept ne obstaja."
                ));

        boolean alreadyFavorite =
                favoriteRepository.existsByUser_IdAndRecipe_Id(
                        user.getId(), recipeId
                );

        if (alreadyFavorite) {
            return;
        }

        FavoriteRecipe favorite = new FavoriteRecipe(user, recipe);
        favoriteRepository.save(favorite);
    }

    // Odstranitev recepta iz priljubljenih
    @Transactional
    public void removeFavorite(AppUser user, Long recipeId) {

        if (!recipeRepository.existsById(recipeId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Recept ne obstaja."
            );
        }

        favoriteRepository.findByUser_IdAndRecipe_Id(
                user.getId(), recipeId
        ).ifPresent(favoriteRepository::delete);
    }

    // Pridobivanje priljubljenih receptov uporabnika
    @Transactional(readOnly = true)
    public List<Recipe> getFavoriteRecipes(AppUser user) {

        return favoriteRepository.findAllByUser_Id(user.getId())
                .stream()
                .map(FavoriteRecipe::getRecipe)
                .toList();
    }
}
