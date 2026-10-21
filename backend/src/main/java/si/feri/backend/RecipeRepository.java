package si.feri.backend;
//sluzi za pristup bazi i nasledjuje jparepository koji ima sve metode za rad
import org.springframework.data.jpa.repository.JpaRepository;
                                        //sa kojom tabelom radimo i tip primarnog kluca
public interface RecipeRepository extends JpaRepository<Recipe, Long> {
}