package com.adb.usermanagementapi.model;

import java.time.LocalDateTime;

public class User {
    private final Long id;
    private final String username;
    private final String email;
    private final String passwordHash;
    private final LocalDateTime createdAt;
    boolean isLocked;

    public User(Long id, String username, String email, String passwordHash,
                LocalDateTime createdAt, boolean isLocked) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.createdAt = createdAt;
        this.isLocked = isLocked;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setLocked(boolean locked) {
        isLocked = locked;
    }

    public boolean isLocked() {
        return isLocked;
    }
}
