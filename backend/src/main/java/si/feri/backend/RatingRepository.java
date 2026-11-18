package si.feri.backend;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RatingRepository extends JpaRepository<Rating, Long> {
    @Modifying
    @Query(value = """
            insert into recipe_ratings (recipe_id, user_id, score)
            values (:recipeId, :userId, :score)
            on conflict (recipe_id, user_id) do nothing
            """, nativeQuery = true)
    int insertRating(@Param("recipeId") Long recipeId, @Param("userId") Long userId,
                     @Param("score") int score);
}
