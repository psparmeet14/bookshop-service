package com.psb.bookshop.identity.dto;

import java.util.Map;
import java.util.UUID;

public record UserProfileResponse(UUID userId, String username) {

    public Map<String, Object> withLinks(String baseUrl) {
        return Map.of(
                "userId", userId,
                "username", username,
                "_links", Map.of(
                        "self",  Map.of("href", baseUrl + "/me/profile"),
                        "books", Map.of("href", baseUrl + "/me/books")
                )
        );
    }
}
