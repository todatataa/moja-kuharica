package si.feri.backend;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecipeRepository extends JpaRepository<Recipe, Long> {
    //iz imena metode sam napravi SQL
    Page<Recipe> findByTitleContainingIgnoreCaseOrIngredientsContainingIgnoreCase(
            String title, String ingredients, Pageable pageable);
}
