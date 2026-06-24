package com.psb.bookshop.identity.infrastructure;

import com.psb.bookshop.identity.domain.User;
import com.psb.bookshop.identity.domain.UserId;
import com.psb.bookshop.identity.domain.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryUserRepository implements UserRepository {

    // Fixed UUIDs so they can be referenced in UserBookAccessRepository
    public static final UUID ALICE_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    public static final UUID BOB_ID   = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");

    private final Map<String, User> store = new ConcurrentHashMap<>();

    public InMemoryUserRepository(PasswordEncoder passwordEncoder) {
        User alice = new User(UserId.of(ALICE_ID), "alice", passwordEncoder.encode("alice123"));
        User bob   = new User(UserId.of(BOB_ID),   "bob",   passwordEncoder.encode("bob123"));
        store.put(alice.getUsername(), alice);
        store.put(bob.getUsername(), bob);
    }

    @Override
    public void save(User user) {
        store.put(user.getUsername(), user);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return Optional.ofNullable(store.get(username));
    }

    @Override
    public boolean existsByUsername(String username) {
        return store.containsKey(username);
    }
}
