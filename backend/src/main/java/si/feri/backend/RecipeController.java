package si.feri.backend;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/recipes") //osnovna adresa svih endpointa
public class RecipeController {

    private final RecipeRepository repository;

    public RecipeController(RecipeRepository repository) {
        this.repository = repository;
    }

    // create
    @PostMapping
    public Recipe create(@RequestBody Recipe recipe) {
        recipe.setId(null);
        return repository.save(recipe);
    }

    // read sve
    @GetMapping
    public List<Recipe> getAll() {
        return repository.findAll();
    }

    // read jedan
    @GetMapping("/{id}")
    public ResponseEntity<Recipe> getOne(@PathVariable Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // update
    @PutMapping("/{id}")
    public ResponseEntity<Recipe> update(@PathVariable Long id, @RequestBody Recipe updated) {
        return repository.findById(id)
                .map(existing -> {
                    existing.setTitle(updated.getTitle());
                    existing.setDescription(updated.getDescription());
                    existing.setIngredients(updated.getIngredients());
                    existing.setInstructions(updated.getInstructions());
                    existing.setPreparationTimeMinutes(updated.getPreparationTimeMinutes());
                    return ResponseEntity.ok(repository.save(existing));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // delete
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}