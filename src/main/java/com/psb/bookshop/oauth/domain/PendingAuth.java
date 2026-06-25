package com.psb.bookshop.oauth.domain;

import java.time.Instant;
import java.util.Set;

/**
 * Represents a user who has successfully logged in but has not yet approved
 * the OAuth consent screen. Stored server-side by a short-lived ticket UUID.
 */
public record PendingAuth(
        String ticket,
        String userId,
        String username,
        String clientId,
        String redirectUri,
        Set<String> requestedScopes,
        String state,
        Instant expiresAt
) {
    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }
}
