package com.adb.usermanagementapi.repository;

import com.adb.usermanagementapi.model.User;

import java.util.List;

public interface UserRepository {
    void save(String username, String email, String passwordHash);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    Long findIdByUsername(String username);
    Long findIdByEmail(String email);
    User findByUsername(String username);
    List<User> findAll();
    void updateUser(User user);
    void updatePassword(String username, String passwordHash);
    void deleteByUsername(String username);
    Boolean isUserLocked(String username);
    void setUserLocked(String username, boolean locked);
}
