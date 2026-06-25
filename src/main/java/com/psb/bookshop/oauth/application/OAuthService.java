package com.psb.bookshop.oauth.application;

import com.psb.bookshop.identity.domain.User;
import com.psb.bookshop.identity.domain.UserRepository;
import com.psb.bookshop.oauth.domain.AuthCode;
import com.psb.bookshop.oauth.domain.OAuthClient;
import com.psb.bookshop.oauth.domain.PendingAuth;
import com.psb.bookshop.oauth.infrastructure.InMemoryAuthCodeStore;
import com.psb.bookshop.oauth.infrastructure.InMemoryOAuthClientStore;
import com.psb.bookshop.oauth.infrastructure.InMemoryPendingAuthStore;
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
    private final InMemoryPendingAuthStore pendingAuthStore;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public OAuthService(InMemoryOAuthClientStore clientStore,
                        InMemoryAuthCodeStore authCodeStore,
                        InMemoryPendingAuthStore pendingAuthStore,
                        UserRepository userRepository,
                        PasswordEncoder passwordEncoder,
                        JwtUtil jwtUtil) {
        this.clientStore = clientStore;
        this.authCodeStore = authCodeStore;
        this.pendingAuthStore = pendingAuthStore;
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

    /**
     * Step 1 of consent flow: validate user credentials and issue a short-lived
     * pending-auth ticket. The ticket is passed to the consent screen so we
     * remember who the user is without re-asking for their password.
     */
    public PendingAuth loginAndCreatePendingAuth(String username, String password,
                                                  String clientId, String redirectUri,
                                                  String scope, String state) {
        OAuthClient client = validateClient(clientId, redirectUri);

        Set<String> requested = Arrays.stream(scope.split(" "))
                .filter(s -> !s.isBlank())
                .collect(Collectors.toSet());

        if (!client.allowedScopes().containsAll(requested)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Requested scope not allowed for this client.");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials."));
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials.");
        }

        PendingAuth pending = new PendingAuth(
                UUID.randomUUID().toString(),
                user.getId().asString(),
                user.getUsername(),
                clientId,
                redirectUri,
                requested,
                state,
                Instant.now().plusSeconds(300)
        );
        pendingAuthStore.save(pending);
        return pending;
    }

    /**
     * Step 2 of consent flow: user has reviewed and approved a subset of scopes.
     * Issues an auth code scoped to only what was approved.
     */
    public AuthCode approveConsent(String ticket, Set<String> approvedScopes) {
        PendingAuth pending = pendingAuthStore.consumeByTicket(ticket)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired consent ticket."));

        if (pending.isExpired()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Consent session expired. Please log in again.");
        }

        // Only grant scopes the user actually approved (subset of what was requested)
        Set<String> granted = pending.requestedScopes().stream()
                .filter(approvedScopes::contains)
                .collect(Collectors.toSet());

        if (granted.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No scopes approved.");
        }

        String grantedScope = String.join(" ", granted);
        AuthCode code = new AuthCode(
                UUID.randomUUID().toString(),
                pending.userId(),
                pending.username(),
                pending.clientId(),
                pending.redirectUri(),
                grantedScope,
                Instant.now().plusSeconds(300)
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
