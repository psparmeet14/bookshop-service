package com.psb.bookshop.catalog.interfaces.rest;

import com.psb.bookshop.catalog.application.dto.BookResponse;
import com.psb.bookshop.catalog.application.usecase.ListBooksUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/books")
public class CatalogController {

    private final ListBooksUseCase listBooksUseCase;

    public CatalogController(ListBooksUseCase listBooksUseCase) {
        this.listBooksUseCase = listBooksUseCase;
    }

    @GetMapping
    public ResponseEntity<List<BookResponse>> listBooks() {
        return ResponseEntity.ok(listBooksUseCase.execute());
    }
}
