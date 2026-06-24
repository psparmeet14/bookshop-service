package com.psb.bookshop.oauth.infrastructure;

import com.psb.bookshop.oauth.domain.OAuthClient;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Repository
public class InMemoryOAuthClientStore {

    private static final Map<String, OAuthClient> CLIENTS = Map.of(
            "bookshop-dashboard", new OAuthClient(
                    "bookshop-dashboard",
                    "dashboard-secret",
                    "http://localhost:3000/callback",
                    Set.of("books:read", "profile:read")
            ),
            "bookshop-mcp", new OAuthClient(
                    "bookshop-mcp",
                    "mcp-secret",
                    "http://localhost:9000/callback",
                    Set.of("books:read", "profile:read")
            )
    );

    public Optional<OAuthClient> findById(String clientId) {
        return Optional.ofNullable(CLIENTS.get(clientId));
    }
}
