package com.psb.bookshop.catalog.domain.model;

import java.math.BigDecimal;

public class Book {

    private final BookId id;
    private final String name;
    private final String author;
    private final BigDecimal price;
    private int availableCount;

    public Book(BookId id, String name, String author, BigDecimal price, int availableCount) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("name must not be blank");
        if (author == null || author.isBlank()) throw new IllegalArgumentException("author must not be blank");
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("price must be >= 0");
        if (availableCount < 0) throw new IllegalArgumentException("availableCount must be >= 0");

        this.id = id;
        this.name = name;
        this.author = author;
        this.price = price;
        this.availableCount = availableCount;
    }

    public BookId getId() { return id; }
    public String getName() { return name; }
    public String getAuthor() { return author; }
    public BigDecimal getPrice() { return price; }
    public int getAvailableCount() { return availableCount; }
    public boolean isAvailable() { return availableCount > 0; }
}
