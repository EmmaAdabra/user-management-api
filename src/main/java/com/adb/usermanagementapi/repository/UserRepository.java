package com.adb.usermanagementapi.repository;

import com.adb.usermanagementapi.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    User save(User user);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    Long findIdByUsername(String username);
    Long findIdByEmail(String email);
    User findByUsername(String username);
    Optional<User> findById(Long id);
    List<User> findAll();
    boolean updateUser(User user);
    boolean updatePassword(String username, String passwordHash);
    boolean deleteByUsername(String username);
    Boolean isUserLocked(String username);
    void setUserLocked(String username, boolean locked);
}
