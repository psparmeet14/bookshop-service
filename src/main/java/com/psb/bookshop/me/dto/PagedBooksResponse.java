package com.psb.bookshop.me.dto;

import com.psb.bookshop.catalog.domain.model.Book;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record PagedBooksResponse(
        List<Map<String, Object>> content,
        Map<String, Object> page,
        Map<String, Object> _links
) {
    public static PagedBooksResponse of(List<Book> books, int pageNum, int size,
                                        int total, int totalPages, String baseUrl) {
        List<Map<String, Object>> content = books.stream()
                .map(b -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", b.getId().value());
                    item.put("name", b.getName());
                    item.put("author", b.getAuthor());
                    item.put("price", b.getPrice());
                    item.put("availableCount", b.getAvailableCount());
                    item.put("_links", Map.of(
                            "self",    Map.of("href", baseUrl + "/books/" + b.getId().value()),
                            "catalog", Map.of("href", baseUrl + "/books")
                    ));
                    return item;
                })
                .toList();

        Map<String, Object> pageMeta = Map.of(
                "number", pageNum,
                "size", size,
                "totalElements", total,
                "totalPages", totalPages
        );

        Map<String, Object> links = new HashMap<>();
        links.put("self",  Map.of("href", pageUrl(baseUrl, pageNum, size)));
        links.put("first", Map.of("href", pageUrl(baseUrl, 0, size)));
        links.put("last",  Map.of("href", pageUrl(baseUrl, Math.max(totalPages - 1, 0), size)));

        if (pageNum > 0) {
            links.put("prev", Map.of("href", pageUrl(baseUrl, pageNum - 1, size)));
        }
        if (pageNum < totalPages - 1) {
            links.put("next", Map.of("href", pageUrl(baseUrl, pageNum + 1, size)));
        }

        return new PagedBooksResponse(content, pageMeta, links);
    }

    private static String pageUrl(String baseUrl, int page, int size) {
        return baseUrl + "/me/books?page=" + page + "&size=" + size;
    }
}
