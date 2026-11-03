package si.feri.backend;

import java.util.List;
//to je kutija u kojoj backend vraca odgovor prije je vraćao običnu listu [] a sada objekt
public record PagedResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
