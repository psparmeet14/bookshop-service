package com.psb.bookshop.me.application;

import com.psb.bookshop.catalog.domain.model.Book;
import com.psb.bookshop.catalog.domain.repository.BookRepository;
import com.psb.bookshop.me.domain.UserBookAccessRepository;
import com.psb.bookshop.me.dto.PagedBooksResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class MyBooksUseCase {

    private final BookRepository bookRepository;
    private final UserBookAccessRepository accessRepository;

    public MyBooksUseCase(BookRepository bookRepository, UserBookAccessRepository accessRepository) {
        this.bookRepository = bookRepository;
        this.accessRepository = accessRepository;
    }

    public PagedBooksResponse execute(UUID userId, int page, int size, String baseUrl) {
        Set<UUID> allowed = accessRepository.findBookIdsByUserId(userId);

        List<Book> userBooks = bookRepository.findAll().stream()
                .filter(b -> allowed.contains(b.getId().value()))
                .toList();

        int total = userBooks.size();
        int totalPages = (int) Math.ceil((double) total / size);
        int from = Math.min(page * size, total);
        int to   = Math.min(from + size, total);
        List<Book> pageSlice = userBooks.subList(from, to);

        return PagedBooksResponse.of(pageSlice, page, size, total, totalPages, baseUrl);
    }
}
