package com.psb.bookshop.me.domain;

import java.util.Set;
import java.util.UUID;

public interface UserBookAccessRepository {
    Set<UUID> findBookIdsByUserId(UUID userId);
}
