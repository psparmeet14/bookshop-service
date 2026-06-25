package com.psb.bookshop.oauth.infrastructure;

import com.psb.bookshop.oauth.domain.PendingAuth;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryPendingAuthStore {

    private final ConcurrentHashMap<String, PendingAuth> store = new ConcurrentHashMap<>();

    public void save(PendingAuth pending) {
        store.put(pending.ticket(), pending);
    }

    /** Consumes (removes) the pending auth — single use. */
    public Optional<PendingAuth> consumeByTicket(String ticket) {
        return Optional.ofNullable(store.remove(ticket));
    }
}
