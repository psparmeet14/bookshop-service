package com.psb.bookshop.oauth.domain;

import java.util.Set;

public record OAuthClient(
        String clientId,
        String clientSecret,
        String redirectUri,
        Set<String> allowedScopes
) {}
