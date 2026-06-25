package com.psb.bookshop.oauth.domain;

import java.util.Arrays;
import java.util.Optional;

public enum OAuthScope {

    BOOKS_READ(
            "books:read",
            "Read your book library",
            "See the books you have access to and their availability"
    ),
    PROFILE_READ(
            "profile:read",
            "Read your profile",
            "See your username and account details"
    );

    private final String value;
    private final String title;
    private final String description;

    OAuthScope(String value, String title, String description) {
        this.value = value;
        this.title = title;
        this.description = description;
    }

    public String getValue()       { return value; }
    public String getTitle()       { return title; }
    public String getDescription() { return description; }

    public static Optional<OAuthScope> fromValue(String value) {
        return Arrays.stream(values())
                .filter(s -> s.value.equals(value))
                .findFirst();
    }
}
