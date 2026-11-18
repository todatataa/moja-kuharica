package si.feri.backend;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;

@RestController
@RequestMapping("/api/recipes")
public class RecipeController {

    private static final Set<String> SORTABLE = Set.of("id", "title", "preparationTimeMinutes");

    private final RecipeRepository repository;
    private final UserRepository userRepository;

    public RecipeController(RecipeRepository repository, UserRepository userRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
    }

    private Long currentUserId(Authentication authentication) {
        return userRepository.findByUsername(authentication.getName())
                .map(AppUser::getId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Uporabnik ne obstaja več."));
    }

    // Recept sme spreminjati njegov avtor. Stari recepti brez avtorja so odprti za vse prijavljene.
    private void requireOwner(Recipe recipe, Long userId) {
        if (recipe.getOwnerId() != null && !recipe.getOwnerId().equals(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Recept lahko ureja ali briše samo njegov avtor.");
        }
    }

    // CREATE
    @Operation(summary = "Doda nov recept")
    @PostMapping
    public Recipe create(@RequestBody Recipe recipe, Authentication authentication) {
        recipe.setId(null);
        // Avtorja vedno določi strežnik, ne odjemalec.
        recipe.setOwnerId(currentUserId(authentication));
        return repository.save(recipe);
    }

    // READ vsi
    @Operation(summary = "Vrne recepte z iskanjem, paginacijo in razvrščanjem")
    @GetMapping
    public PagedResponse<Recipe> getAll(
            @Parameter(description = "Iskalni niz (išče po naslovu in sestavinah)")
            @RequestParam(defaultValue = "") String q, //za search string iz urla npr jaja
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
    @Operation(summary = "Posodobi obstoječi recept (samo avtor)")
    @PutMapping("/{id}")
    public ResponseEntity<Recipe> update(@PathVariable Long id, @RequestBody Recipe updated,
                                         Authentication authentication) {
        Long userId = currentUserId(authentication);
        return repository.findById(id)
                .map(existing -> {
                    requireOwner(existing, userId);
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
    @Operation(summary = "Izbriše recept (samo avtor)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication) {
        Long userId = currentUserId(authentication);
        return repository.findById(id)
                .map(existing -> {
                    requireOwner(existing, userId);
                    repository.delete(existing);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
