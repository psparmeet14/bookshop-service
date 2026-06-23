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

    private static final List<Book> BOOKS = List.of(
            new Book(BookId.of(UUID.randomUUID()), "Clean Code", "Robert C. Martin", new BigDecimal("29.99"), 5),
            new Book(BookId.of(UUID.randomUUID()), "Domain-Driven Design", "Eric Evans", new BigDecimal("44.99"), 3),
            new Book(BookId.of(UUID.randomUUID()), "The Pragmatic Programmer", "David Thomas", new BigDecimal("39.99"), 7),
            new Book(BookId.of(UUID.randomUUID()), "Designing Data-Intensive Applications", "Martin Kleppmann", new BigDecimal("49.99"), 2),
            new Book(BookId.of(UUID.randomUUID()), "Refactoring", "Martin Fowler", new BigDecimal("34.99"), 0)
    );

    @Override
    public List<Book> findAll() {
        return BOOKS;
    }
}
