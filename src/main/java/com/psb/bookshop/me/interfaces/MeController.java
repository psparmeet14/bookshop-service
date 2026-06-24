package com.psb.bookshop.me.interfaces;

import com.psb.bookshop.identity.application.AuthService;
import com.psb.bookshop.identity.dto.UserProfileResponse;
import com.psb.bookshop.me.application.MyBooksUseCase;
import com.psb.bookshop.me.dto.PagedBooksResponse;
import com.psb.bookshop.shared.security.BookshopPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/me")
public class MeController {

    private final AuthService authService;
    private final MyBooksUseCase myBooksUseCase;

    public MeController(AuthService authService, MyBooksUseCase myBooksUseCase) {
        this.authService = authService;
        this.myBooksUseCase = myBooksUseCase;
    }

    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> profile(
            @AuthenticationPrincipal BookshopPrincipal principal,
            HttpServletRequest request) {
        UserProfileResponse profile = authService.profile(principal.userId(), principal.username());
        String baseUrl = baseUrl(request);
        return ResponseEntity.ok(profile.withLinks(baseUrl));
    }

    @GetMapping("/books")
    public ResponseEntity<PagedBooksResponse> myBooks(
            @AuthenticationPrincipal BookshopPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        PagedBooksResponse response = myBooksUseCase.execute(
                UUID.fromString(principal.userId()), page, size, baseUrl(request));
        return ResponseEntity.ok(response);
    }

    private String baseUrl(HttpServletRequest req) {
        return req.getScheme() + "://" + req.getServerName() + ":" + req.getServerPort();
    }
}
