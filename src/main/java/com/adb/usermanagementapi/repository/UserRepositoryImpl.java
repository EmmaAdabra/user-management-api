package com.adb.usermanagementapi.repository;

import com.adb.usermanagementapi.exception.UserNotFoundException;
import com.adb.usermanagementapi.model.User;
import com.adb.usermanagementapi.util.UserSql;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;

public class UserRepositoryImpl implements UserRepository
{
    private final JdbcTemplate jdbcTemplate;
    private static final RowMapper<User> USER_ROW_MAPPER = (rs, rowNum) -> new User(
                rs.getLong("id"),
                rs.getString("username"),
                rs.getString("email"),
                rs.getString("password_hash"),
                rs.getTimestamp("created_at").toLocalDateTime(),
                rs.getBoolean("is_locked")
        );

    public UserRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public User save(User user) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(UserSql.INSERT_INTO_USERS,
                    new String[]{"id"});
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPasswordHash());

            return ps;
        }, keyHolder);

        Long generatedId = keyHolder.getKey().longValue();

        return new User(generatedId, user.getUsername(), user.getEmail(), user.getPasswordHash(),
                user.getCreatedAt(), user.isLocked());
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
    public Optional<User> findById(Long id) {
        User user;
        try {
            user = jdbcTemplate.queryForObject(UserSql.SELECT_USER_BY_ID, USER_ROW_MAPPER,
                    id);
        } catch (org.springframework.dao.EmptyResultDataAccessException e){
            user = null;
        }
        return Optional.ofNullable(user);
    }


    @Override
    public Optional<User> findByEmail(String email) {
        User user;
        try {
            user = jdbcTemplate.queryForObject(UserSql.SELECT_USER_BY_EMAIL, USER_ROW_MAPPER,
                    email);
        } catch (org.springframework.dao.EmptyResultDataAccessException e){
            user = null;
        }
        return Optional.ofNullable(user);
    }

    @Override
    public List<User> findAll() {
        return jdbcTemplate.query(UserSql.SELECT_ALL_USERS, USER_ROW_MAPPER);
    }

    @Override
    public boolean updateUser(User user) {
        int count = jdbcTemplate.update(UserSql.UPDATE_USER_USERNAME_EMAIL_BY_ID, user.getUsername(),
                user.getEmail(), user.getId());

        return count > 0;
    }

    @Override
    public boolean updatePassword(Long id, String passwordHash) {
        int count = jdbcTemplate.update(UserSql.UPDATE_USER_PASSWORD_BY_ID, passwordHash, id);

        return count > 0;
    }

    @Override
    public boolean deleteUser(Long id) {
        int count = jdbcTemplate.update(UserSql.DELETE_USER_BY_ID, id);

        return count > 0;
    }

    @Override
    public Boolean isUserLocked(Long id) {
        try{
            return jdbcTemplate.queryForObject(UserSql.IS_USER_LOCKED, Boolean.class, id);
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public void setUserLocked(Long id, boolean lockedStatus) {
        int rows = jdbcTemplate.update(UserSql.UPDATE_USER_LOCK_STATUS, lockedStatus, id);
        if (rows == 0) {
            throw new UserNotFoundException("User not found: " + id);
        }
    }
}
