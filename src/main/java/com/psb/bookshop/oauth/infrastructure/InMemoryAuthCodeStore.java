package com.psb.bookshop.oauth.infrastructure;

import com.psb.bookshop.oauth.domain.AuthCode;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryAuthCodeStore {

    private final ConcurrentHashMap<String, AuthCode> store = new ConcurrentHashMap<>();

    public void save(AuthCode authCode) {
        store.put(authCode.code(), authCode);
    }

    public Optional<AuthCode> consumeByCode(String code) {
        return Optional.ofNullable(store.remove(code));
    }
}
