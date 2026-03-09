package com.flightbookingapp.util;

import com.flightbookingapp.dto.response.PagedResponse;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Converts Spring Data {@link Page} objects into the API's {@link PagedResponse} envelope.
 */
public final class PageUtils {

    private PageUtils() { /* utility class */ }

    public static <T, R> PagedResponse<R> toPagedResponse(Page<T> page,
                                                            Function<T, R> mapper) {
        List<R> content = page.getContent()
                              .stream()
                              .map(mapper)
                              .collect(Collectors.toList());

        return PagedResponse.<R>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }
}
