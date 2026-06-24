package com.psb.bookshop.identity.domain;

import java.util.Optional;

public interface UserRepository {
    void save(User user);
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
}
