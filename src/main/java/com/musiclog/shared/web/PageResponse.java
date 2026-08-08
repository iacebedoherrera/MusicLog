package com.musiclog.shared.web;

import java.util.List;
import org.springframework.data.domain.Page;

/**
 * Uniform, zero-based pagination envelope for collection endpoints.
 *
 * @param items items in the requested page
 * @param page zero-based page number
 * @param size page size, between 1 and 50
 * @param totalElements total number of matching items when available
 * @param totalPages number of available pages
 * @param hasNext whether a subsequent page is available
 */
public record PageResponse<T>(
        List<T> items,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext
) {
    public static <T> PageResponse<T> from(Page<T> source) {
        return new PageResponse<>(
                source.getContent(),
                source.getNumber(),
                source.getSize(),
                source.getTotalElements(),
                source.getTotalPages(),
                source.hasNext());
    }

    public static <T> PageResponse<T> from(Page<?> source, List<T> items) {
        return new PageResponse<>(
                items,
                source.getNumber(),
                source.getSize(),
                source.getTotalElements(),
                source.getTotalPages(),
                source.hasNext());
    }

    public static <T> PageResponse<T> of(List<T> items, int page, int size, long totalElements) {
        long normalizedTotal = Math.max(totalElements, items.size());
        int totalPages = normalizedTotal == 0 ? 0 : (int) Math.ceil((double) normalizedTotal / size);
        return new PageResponse<>(items, page, size, normalizedTotal, totalPages, page + 1 < totalPages);
    }
}
