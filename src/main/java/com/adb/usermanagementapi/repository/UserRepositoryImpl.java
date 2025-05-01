package com.adb.usermanagementapi.repository;

import com.adb.usermanagementapi.model.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.util.List;

public class UserRepositoryImpl implements UserRepository
{
    private final JdbcTemplate jdbcTemplate;
    private RowMapper<User> userRowMapper(){
        return (rs, rowNum) -> new User(
                rs.getLong("id"),
                rs.getString("username"),
                rs.getString("email"),
                rs.getString("password_hash"),
                rs.getTimestamp("created_at").toLocalDateTime()
        );
    }

    public UserRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(String username, String email, String passwordHash) {
        String sql = "INSERT INTO users (username, email, password_hash) VALUES(?, ?, ?)";

        jdbcTemplate.update(sql, username, email, passwordHash);
    }

    @Override
    public boolean existsByUsername(String username) {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
        Integer count =  jdbcTemplate.queryForObject(sql, Integer.class, username);

        return count != null && count > 0;
    }

    @Override
    public boolean existsByEmail(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
        Integer count =  jdbcTemplate.queryForObject(sql, Integer.class, email);

        return count != null && count > 0;
    }

    @Override
    public Long findIdByUsername(String username) {
        String sql = "SELECT id from users WHERE username = ?";

        try {
            return jdbcTemplate.queryForObject(sql, Long.class, username);
        } catch (org.springframework.dao.EmptyResultDataAccessException e){
            return null;
        }
    }

    @Override
    public Long findIdByEmail(String email) {
        String sql = "SELECT id from users WHERE email = ?";

        try {
            return jdbcTemplate.queryForObject(sql, Long.class, email);
        } catch (org.springframework.dao.EmptyResultDataAccessException e){
            return null;
        }
    }

    @Override
    public User findByUsername(String username) {
        String sql = "SELECT id, username, email, password_hash, created_at FROM users WHERE username = ?";

        try {
            return jdbcTemplate.queryForObject(sql, userRowMapper(), username);
        } catch (org.springframework.dao.EmptyResultDataAccessException e){
            return null;
        }
    }

    @Override
    public List<User> findAll() {
        String sql = "SELECT id, username, email, password_hash, created_at FROM users";

        try {
            return jdbcTemplate.query(sql, userRowMapper());
        } catch (org.springframework.dao.EmptyResultDataAccessException e){
            return null;
        }
    }

    @Override
    public void updateUser(User user) {
        String sql = "UPDATE users SET username = ?, email = ? WHERE id = ?";
        int count = jdbcTemplate.update(sql, user.getUsername(), user.getEmail(), user.getId());

        if(count == 0){
            throw new IllegalArgumentException("User with ID - " + user.getId() + " not found");
        }
    }

    @Override
    public void updatePassword(String username, String passwordHash) {
        String sql = "UPDATE users SET password_hash = ? WHERE username = ?";
        int count = jdbcTemplate.update(sql, passwordHash, username);

        if(count == 0){
            throw new IllegalArgumentException("User with username - " + username + " not found");
        }
    }

    @Override
    public void deleteUser(String username) {
        String sql = "DELETE FROM users WHERE username = ?";
        int count = jdbcTemplate.update(sql, username);

        if(count == 0){
            throw new IllegalArgumentException("user with username - " + username + " not found");
        }
    }
}
