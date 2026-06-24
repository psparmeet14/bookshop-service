package com.psb.bookshop.oauth.domain;

import java.time.Instant;

public record AuthCode(
        String code,
        String userId,
        String username,
        String clientId,
        String redirectUri,
        String scope,
        Instant expiresAt
) {
    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }
}
