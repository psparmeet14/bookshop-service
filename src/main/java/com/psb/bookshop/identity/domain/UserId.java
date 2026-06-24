package com.psb.bookshop.identity.domain;

import java.util.UUID;

public record UserId(UUID value) {
    public static UserId of(UUID value) { return new UserId(value); }
    public String asString() { return value.toString(); }
}
