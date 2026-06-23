package com.psb.bookshop.catalog.domain.model;

import java.util.UUID;

public record BookId(UUID value) {

    public static BookId of(UUID value) {
        return new BookId(value);
    }
}
