package si.feri.backend;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import org.hibernate.annotations.Formula;

import java.math.BigDecimal;

@Entity //klasa recipe je tabela u bazi
public class Recipe {

    @Id //primarni kljuc koji baza sama povecava
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(length = 2000)
    private String description;

    @Column(length = 4000)
    private String ingredients;

    @Column(length = 4000)
    private String instructions;

    private int preparationTimeMinutes;

    // Id avtorja recepta. Stari recepti iz časa pred prijavo ga nimajo (null).
    private Long ownerId;

    @Formula("(select round(avg(r.score), 1) from recipe_ratings r where r.recipe_id = id)")
    private BigDecimal averageRating;

    @Formula("(select count(*) from recipe_ratings r where r.recipe_id = id)")
    private long ratingCount;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public BigDecimal getAverageRating() { return averageRating; }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public long getRatingCount() { return ratingCount; }

    public Recipe() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getIngredients() { return ingredients; }
    public void setIngredients(String ingredients) { this.ingredients = ingredients; }

    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }

    public int getPreparationTimeMinutes() { return preparationTimeMinutes; }
    public void setPreparationTimeMinutes(int preparationTimeMinutes) { this.preparationTimeMinutes = preparationTimeMinutes; }

    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }
}
