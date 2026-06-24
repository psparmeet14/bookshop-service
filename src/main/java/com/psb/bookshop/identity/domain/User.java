package com.psb.bookshop.identity.domain;

public class User {

    private final UserId id;
    private final String username;
    private final String passwordHash;

    public User(UserId id, String username, String passwordHash) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
    }

    public UserId getId() { return id; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
}
