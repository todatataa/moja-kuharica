package si.feri.backend;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/recipes")
public class RecipeController {

    private static final Set<String> SORTABLE = Set.of("id", "title", "preparationTimeMinutes");

    private final RecipeRepository repository;

    public RecipeController(RecipeRepository repository) {
        this.repository = repository;
    }

    // CREATE
    @Operation(summary = "Doda nov recept")
    @PostMapping
    public Recipe create(@RequestBody Recipe recipe) {
        recipe.setId(null);
        return repository.save(recipe);
    }

    // READ - vsi (z iskanjem, paginacijo in razvrščanjem)
    @Operation(summary = "Vrne recepte z iskanjem, paginacijo in razvrščanjem")
    @GetMapping
    public PagedResponse<Recipe> getAll(
            @Parameter(description = "Iskalni niz (išče po naslovu in sestavinah)")
            @RequestParam(defaultValue = "") String q,
            @Parameter(description = "Številka strani (začne se pri 0)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Število receptov na stran")
            @RequestParam(defaultValue = "5") int size,
            @Parameter(description = "Polje razvrščanja: id, title ali preparationTimeMinutes")
            @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Smer razvrščanja: asc ali desc")
            @RequestParam(defaultValue = "desc") String direction) {

        String sortField = SORTABLE.contains(sortBy) ? sortBy : "id";
        Sort.Direction dir = "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(dir, sortField);
        if (!sortField.equals("id")) {
            sort = sort.and(Sort.by(Sort.Direction.ASC, "id"));
        }

        PageRequest pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50), sort);
        String term = q.trim();

        Page<Recipe> result = repository
                .findByTitleContainingIgnoreCaseOrIngredientsContainingIgnoreCase(term, term, pageable);

        return new PagedResponse<>(
                result.getContent(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages());
    }

    // READ - jedan
    @Operation(summary = "Vrne en recept po id-ju")
    @GetMapping("/{id}")
    public ResponseEntity<Recipe> getOne(@PathVariable Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // UPDATE
    @Operation(summary = "Posodobi obstoječi recept")
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

    // DELETE
    @Operation(summary = "Izbriše recept")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
