package com.adb.usermanagementapi.repository;

import com.adb.usermanagementapi.model.User;

import java.util.List;

public interface UserRepository {
    User save(User user);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    Long findIdByUsername(String username);
    Long findIdByEmail(String email);
    User findByUsername(String username);
    List<User> findAll();
    boolean updateUser(String username, String email, Long userId);
    boolean updatePassword(String username, String passwordHash);
    boolean deleteByUsername(String username);
    Boolean isUserLocked(String username);
    void setUserLocked(String username, boolean locked);
}
