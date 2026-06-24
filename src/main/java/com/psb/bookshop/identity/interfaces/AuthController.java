package com.psb.bookshop.identity.interfaces;

import com.psb.bookshop.identity.application.AuthService;
import com.psb.bookshop.identity.dto.LoginRequest;
import com.psb.bookshop.identity.dto.RegisterRequest;
import com.psb.bookshop.identity.dto.UserProfileResponse;
import com.psb.bookshop.shared.security.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;

    public AuthController(AuthService authService, JwtUtil jwtUtil) {
        this.authService = authService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<UserProfileResponse> register(@RequestBody RegisterRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(req));
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest req) {
        String token = authService.login(req);
        return ResponseEntity.ok(Map.of(
                "access_token", token,
                "token_type", "Bearer",
                "expires_in", jwtUtil.expiryMs() / 1000,
                "scope", AuthService.DEFAULT_SCOPE
        ));
    }
}
