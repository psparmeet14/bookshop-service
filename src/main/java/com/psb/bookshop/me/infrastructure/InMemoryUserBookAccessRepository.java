package com.psb.bookshop.me.infrastructure;

import com.psb.bookshop.catalog.infrastructure.persistence.InMemoryBookRepository;
import com.psb.bookshop.identity.infrastructure.InMemoryUserRepository;
import com.psb.bookshop.me.domain.UserBookAccessRepository;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Repository
public class InMemoryUserBookAccessRepository implements UserBookAccessRepository {

    // alice  → Clean Code, Refactoring
    // bob    → DDD, Pragmatic Programmer, DDIA
    private static final Map<UUID, Set<UUID>> ACCESS = Map.of(
            InMemoryUserRepository.ALICE_ID, Set.of(
                    InMemoryBookRepository.CLEAN_CODE_ID,
                    InMemoryBookRepository.REFACTORING_ID
            ),
            InMemoryUserRepository.BOB_ID, Set.of(
                    InMemoryBookRepository.DDD_ID,
                    InMemoryBookRepository.PRAGMATIC_ID,
                    InMemoryBookRepository.DDIA_ID
            )
    );

    @Override
    public Set<UUID> findBookIdsByUserId(UUID userId) {
        return ACCESS.getOrDefault(userId, Set.of());
    }
}
