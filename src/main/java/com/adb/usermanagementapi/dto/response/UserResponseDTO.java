package com.adb.usermanagementapi.dto.response;

import java.time.LocalDateTime;

public class UserResponseDTO {
    private Long id;
    private String username;
    private String email;
    private boolean isLocked;
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    // constructor
    public UserResponseDTO(Long id, String username, String email, boolean isLocked,
                           LocalDateTime createdAt)
    {
        this.id = id;
        this.username = username;
        this.email = email;
        this.isLocked = isLocked;
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "UserResponseDTO{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", isLocked=" + isLocked +
                ", createdAt=" + createdAt +
                '}';
    }

    // getters and setters
    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public void setLocked(boolean locked) {
        isLocked = locked;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
