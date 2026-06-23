package com.psb.bookshop.catalog.application.usecase;

import com.psb.bookshop.catalog.application.dto.BookResponse;
import com.psb.bookshop.catalog.domain.repository.BookRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListBooksUseCase {

    private final BookRepository bookRepository;

    public ListBooksUseCase(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public List<BookResponse> execute() {
        return bookRepository.findAll().stream()
                .map(book -> new BookResponse(
                        book.getId().value(),
                        book.getName(),
                        book.getAuthor(),
                        book.getPrice(),
                        book.getAvailableCount()
                ))
                .toList();
    }
}
