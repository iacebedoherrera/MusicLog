package com.musiclog.shared.web;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

/** Shared pagination limits for all public collection endpoints. */
public final class Pagination {

    public static final int DEFAULT_SIZE = 20;
    public static final int MAX_SIZE = 50;

    private Pagination() {
    }

    public static Pageable pageable(int page, int size) {
        return PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), MAX_SIZE));
    }
}
