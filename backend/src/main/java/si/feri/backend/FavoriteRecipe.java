
package si.feri.backend;

import jakarta.persistence.*;

@Entity
@Table(
        name = "favorite_recipes",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_favorite_user_recipe",
                columnNames = {"user_id", "recipe_id"}
        )
)
public class FavoriteRecipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipe_id", nullable = false)
    private Recipe recipe;

    public FavoriteRecipe() {
    }

    public FavoriteRecipe(AppUser user, Recipe recipe) {
        this.user = user;
        this.recipe = recipe;
    }

    public Long getId() {
        return id;
    }

    public AppUser getUser() {
        return user;
    }

    public void setUser(AppUser user) {
        this.user = user;
    }

    public Recipe getRecipe() {
        return recipe;
    }

    public void setRecipe(Recipe recipe) {
        this.recipe = recipe;
    }
}
