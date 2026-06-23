package com.psb.bookshop.catalog.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record BookResponse(
        UUID id,
        String name,
        String author,
        BigDecimal price,
        int availableCount
) {}
