package com.japaneselearning.common.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Paginated response wrapper.
 * Used for endpoints that return paginated lists.
 *
 * @param <T> type of elements in the data list
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {

    private int currentPage;
    private int pageSize;
    private long totalPages;
    private long totalElements;
    private List<T> data;
}
