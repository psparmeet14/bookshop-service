package com.psb.bookshop.oauth.application;

import com.psb.bookshop.identity.domain.User;
import com.psb.bookshop.identity.domain.UserRepository;
import com.psb.bookshop.oauth.domain.AuthCode;
import com.psb.bookshop.oauth.domain.OAuthClient;
import com.psb.bookshop.oauth.infrastructure.InMemoryAuthCodeStore;
import com.psb.bookshop.oauth.infrastructure.InMemoryOAuthClientStore;
import com.psb.bookshop.shared.security.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OAuthService {

    private final InMemoryOAuthClientStore clientStore;
    private final InMemoryAuthCodeStore authCodeStore;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public OAuthService(InMemoryOAuthClientStore clientStore,
                        InMemoryAuthCodeStore authCodeStore,
                        UserRepository userRepository,
                        PasswordEncoder passwordEncoder,
                        JwtUtil jwtUtil) {
        this.clientStore = clientStore;
        this.authCodeStore = authCodeStore;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public OAuthClient validateClient(String clientId, String redirectUri) {
        OAuthClient client = clientStore.findById(clientId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown client_id."));
        if (!client.redirectUri().equals(redirectUri)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "redirect_uri mismatch.");
        }
        return client;
    }

    public AuthCode issueCode(String clientId, String redirectUri, String scope,
                               String username, String password) {
        OAuthClient client = validateClient(clientId, redirectUri);

        Set<String> requested = Arrays.stream(scope.split(" ")).collect(Collectors.toSet());
        if (!client.allowedScopes().containsAll(requested)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Requested scope not allowed for this client.");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials."));
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials.");
        }

        AuthCode code = new AuthCode(
                UUID.randomUUID().toString(),
                user.getId().asString(),
                user.getUsername(),
                clientId,
                redirectUri,
                scope,
                Instant.now().plusSeconds(300) // 5-minute window
        );
        authCodeStore.save(code);
        return code;
    }

    public String exchangeCodeForToken(String code, String clientId,
                                        String clientSecret, String redirectUri) {
        OAuthClient client = clientStore.findById(clientId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unknown client."));
        if (!client.clientSecret().equals(clientSecret)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid client_secret.");
        }

        AuthCode authCode = authCodeStore.consumeByCode(code)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or already used code."));

        if (authCode.isExpired()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Auth code has expired.");
        }
        if (!authCode.clientId().equals(clientId) || !authCode.redirectUri().equals(redirectUri)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Code was not issued for this client/redirect_uri.");
        }

        return jwtUtil.issue(authCode.userId(), authCode.username(), authCode.scope());
    }
}
