package com.adb.usermanagementapi.repository;

import com.adb.usermanagementapi.exception.UserNotFoundException;
import com.adb.usermanagementapi.model.User;
import com.adb.usermanagementapi.util.UserSql;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.util.List;

public class UserRepositoryImpl implements UserRepository
{
    private final JdbcTemplate jdbcTemplate;
    private static final RowMapper<User> USER_ROW_MAPPER = (rs, rowNum) -> new User(
                rs.getLong("id"),
                rs.getString("username"),
                rs.getString("email"),
                rs.getString("password_hash"),
                rs.getTimestamp("created_at").toLocalDateTime()
        );

    public UserRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(String username, String email, String passwordHash) {

        jdbcTemplate.update(UserSql.INSERT_INTO_USERS, username, email, passwordHash);
    }

    @Override
    public boolean existsByUsername(String username) {
        int count;

        try {
            count =  jdbcTemplate.queryForObject(UserSql.CHECK_USER_EXISTS_BY_USERNAME, Integer.class, username);
        } catch (org.springframework.dao.EmptyResultDataAccessException e){
            count = 0;
        }

        return count > 0;
    }

    @Override
    public boolean existsByEmail(String email) {
        int count;

        try {
            count =  jdbcTemplate.queryForObject(UserSql.CHECK_USER_EXISTS_BY_EMAIL, Integer.class, email);
        } catch (org.springframework.dao.EmptyResultDataAccessException e){
            count = 0;
        }

        return count > 0;
    }

    @Override
    public Long findIdByUsername(String username) {
        try {
            return jdbcTemplate.queryForObject(UserSql.SELECT_USER_ID_BY_USERNAME, Long.class, username);
        } catch (org.springframework.dao.EmptyResultDataAccessException e){
            return null;
        }
    }

    @Override
    public Long findIdByEmail(String email) {
        try {
            return jdbcTemplate.queryForObject(UserSql.SELECT_USER_ID_BY_EMAIL, Long.class, email);
        } catch (org.springframework.dao.EmptyResultDataAccessException e){
            return null;
        }
    }

    @Override
    public User findByUsername(String username) {
        try {
            return jdbcTemplate.queryForObject(UserSql.SELECT_USER_BY_USERNAME, USER_ROW_MAPPER, username);
        } catch (org.springframework.dao.EmptyResultDataAccessException e){
            return null;
        }
    }

    @Override
    public List<User> findAll() {
        return jdbcTemplate.query(UserSql.SELECT_ALL_USERS, USER_ROW_MAPPER);
    }

    @Override
    public void updateUser(User user) {
        int count = jdbcTemplate.update(UserSql.UPDATE_USER_USERNAME_EMAIL_BY_ID, user.getUsername(), user.getEmail(),
                user.getId());

        if(count == 0){
            throw new UserNotFoundException("User with ID - " + user.getId() + " not found");
        }
    }

    @Override
    public void updatePassword(String username, String passwordHash) {
        int count = jdbcTemplate.update(UserSql.UPDATE_USER_PASSWORD_BY_USERNAME, passwordHash, username);

        if(count == 0){
            throw new UserNotFoundException("User with username - " + username + " not found");
        }
    }

    @Override
    public void deleteByUsername(String username) {
        int count = jdbcTemplate.update(UserSql.DELETE_USER_BY_USERNAME, username);

        if(count == 0){
            throw new UserNotFoundException("user with username - " + username + " not found");
        }
    }

    @Override
    public Boolean isUserLocked(String username) {
        try{
            return jdbcTemplate.queryForObject(UserSql.IS_USER_LOCKED, Boolean.class, username);
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public void setUserLocked(String username, boolean lockedStatus) {
        int rows = jdbcTemplate.update(UserSql.UPDATE_USER_LOCK_STATUS, lockedStatus, username);
        if (rows == 0) {
            throw new UserNotFoundException("User not found: " + username);
        }
    }
}
