package com.psb.bookshop.catalog.infrastructure.persistence;

import com.psb.bookshop.catalog.domain.model.Book;
import com.psb.bookshop.catalog.domain.model.BookId;
import com.psb.bookshop.catalog.domain.repository.BookRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public class InMemoryBookRepository implements BookRepository {

    // Fixed UUIDs so UserBookAccessRepository can reference them by ID
    public static final UUID CLEAN_CODE_ID   = UUID.fromString("11111111-1111-1111-1111-111111111111");
    public static final UUID DDD_ID          = UUID.fromString("22222222-2222-2222-2222-222222222222");
    public static final UUID PRAGMATIC_ID    = UUID.fromString("33333333-3333-3333-3333-333333333333");
    public static final UUID DDIA_ID         = UUID.fromString("44444444-4444-4444-4444-444444444444");
    public static final UUID REFACTORING_ID  = UUID.fromString("55555555-5555-5555-5555-555555555555");

    private static final List<Book> BOOKS = List.of(
            new Book(BookId.of(CLEAN_CODE_ID),  "Clean Code",                          "Robert C. Martin", new BigDecimal("29.99"), 5),
            new Book(BookId.of(DDD_ID),          "Domain-Driven Design",                "Eric Evans",       new BigDecimal("44.99"), 3),
            new Book(BookId.of(PRAGMATIC_ID),    "The Pragmatic Programmer",            "David Thomas",     new BigDecimal("39.99"), 7),
            new Book(BookId.of(DDIA_ID),         "Designing Data-Intensive Applications","Martin Kleppmann",new BigDecimal("49.99"), 2),
            new Book(BookId.of(REFACTORING_ID),  "Refactoring",                         "Martin Fowler",    new BigDecimal("34.99"), 0)
    );

    @Override
    public List<Book> findAll() {
        return BOOKS;
    }
}
