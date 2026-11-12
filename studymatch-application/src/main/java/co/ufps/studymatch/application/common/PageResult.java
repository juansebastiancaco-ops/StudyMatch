package co.ufps.studymatch.application.common;

import java.util.List;

public record PageResult<T>(
        List<T> items,
        int page,
        int size,
        long total,
        int totalPages
) {
    public static <T> PageResult<T> of(List<T> items, int page, int size, long total) {
        int totalPages = size <= 0 ? 0 : (int) Math.ceil(total / (double) size);
        return new PageResult<>(List.copyOf(items), page, size, total, totalPages);
    }
}
