package si.feri.backend;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;

@Entity
@Table(name = "recipe_comments", indexes = @Index(
        name = "idx_recipe_comments_order", columnList = "recipe_id, created_at DESC, id DESC"))
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String text;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipe_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Recipe recipe;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private AppUser author;

    protected Comment() {}

    public Comment(String text, Instant createdAt, Recipe recipe, AppUser author) {
        this.text = text;
        this.createdAt = createdAt;
        this.recipe = recipe;
        this.author = author;
    }

    public Long getId() { return id; }
    public String getText() { return text; }
    public Instant getCreatedAt() { return createdAt; }
    public AppUser getAuthor() { return author; }
}
