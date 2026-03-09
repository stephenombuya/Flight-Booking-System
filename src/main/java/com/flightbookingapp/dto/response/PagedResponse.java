package com.flightbookingapp.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Wraps paginated data with metadata for the client.
 *
 * @param <T> type of the content items
 */
@Data
@Builder
public class PagedResponse<T> {
    private List<T> content;
    private int     page;
    private int     size;
    private long    totalElements;
    private int     totalPages;
    private boolean last;
}
