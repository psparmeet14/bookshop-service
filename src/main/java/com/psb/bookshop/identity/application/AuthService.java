package com.psb.bookshop.identity.application;

import com.psb.bookshop.identity.domain.User;
import com.psb.bookshop.identity.domain.UserId;
import com.psb.bookshop.identity.domain.UserRepository;
import com.psb.bookshop.identity.dto.LoginRequest;
import com.psb.bookshop.identity.dto.RegisterRequest;
import com.psb.bookshop.identity.dto.UserProfileResponse;
import com.psb.bookshop.shared.security.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
public class AuthService {

    public static final String DEFAULT_SCOPE = "books:read profile:read";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public UserProfileResponse register(RegisterRequest req) {
        if (userRepository.existsByUsername(req.username())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already taken.");
        }
        User user = new User(UserId.of(UUID.randomUUID()), req.username(), passwordEncoder.encode(req.password()));
        userRepository.save(user);
        return toProfile(user);
    }

    public String login(LoginRequest req) {
        User user = userRepository.findByUsername(req.username())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials."));
        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials.");
        }
        return jwtUtil.issue(user.getId().asString(), user.getUsername(), DEFAULT_SCOPE);
    }

    public UserProfileResponse profile(String userId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found."));
        return toProfile(user);
    }

    private UserProfileResponse toProfile(User user) {
        return new UserProfileResponse(user.getId().value(), user.getUsername());
    }
}
