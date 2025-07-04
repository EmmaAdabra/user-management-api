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
    Optional<User> findByEmail(String email);
    List<User> findAll(int page, int size);
    boolean updateUser(User user);
    boolean updatePassword(Long id, String passwordHash);
    boolean deleteUser(Long id);
    Boolean isUserLocked(Long id);
    void setUserLocked(Long id, boolean locked);
}
