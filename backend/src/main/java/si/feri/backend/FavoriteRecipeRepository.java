
package si.feri.backend;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface FavoriteRecipeRepository
        extends JpaRepository<FavoriteRecipe, Long> {

    List<FavoriteRecipe> findAllByUser_Id(Long userId);

    boolean existsByUser_IdAndRecipe_Id(
            Long userId, Long recipeId
    );

    Optional<FavoriteRecipe> findByUser_IdAndRecipe_Id(
            Long userId, Long recipeId
    );
}
