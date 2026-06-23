package com.musiclog.catalog.dto;

import java.util.List;

public record SearchResponse(
        String type,
        List<SearchItem> results
) {
    public record SearchItem(
            String mbid,
            String title,
            String subtitle,
            Integer score
    ) {
    }
}
