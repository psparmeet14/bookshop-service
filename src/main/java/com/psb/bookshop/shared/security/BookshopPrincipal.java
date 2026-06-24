package com.psb.bookshop.shared.security;

public record BookshopPrincipal(String userId, String username, String scope) {}
