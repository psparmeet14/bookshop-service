package com.psb.bookshop.catalog.domain.repository;

import com.psb.bookshop.catalog.domain.model.Book;

import java.util.List;

public interface BookRepository {
    List<Book> findAll();
}
