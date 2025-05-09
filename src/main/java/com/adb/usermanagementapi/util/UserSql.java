package com.adb.usermanagementapi.util;

public class UserSql {
    public static final String INSERT_INTO_USERS = "INSERT INTO users (username, email, " +
            "password_hash) VALUES(?, ?, ?)";
    public static final String CHECK_USER_EXISTS_BY_USERNAME = "SELECT 1 FROM users WHERE " +
            "username = ? LIMIT 1";
    public static final String CHECK_USER_EXISTS_BY_EMAIL = "SELECT 1 FROM users WHERE email = ? " +
            "LIMIT 1";
    public static final String SELECT_USER_ID_BY_USERNAME = "SELECT id from users WHERE username " +
            "= ? LIMIT 1";
    public static final String SELECT_USER_ID_BY_EMAIL = "SELECT id from users WHERE email " +
            "= ? LIMIT 1";
    public static final String SELECT_USER_BY_USERNAME = "SELECT id, username, email, " +
            "password_hash, created_at, is_locked FROM users WHERE username = ?";
    public static final String SELECT_ALL_USERS = "SELECT id, username, email, password_hash, " +
            "created_at, is_locked FROM users";
    public static final String UPDATE_USER_USERNAME_EMAIL_BY_ID = "UPDATE users SET username = ?," +
            " email = ? WHERE id = ?";
    public static final String UPDATE_USER_PASSWORD_BY_USERNAME = "UPDATE users SET password_hash" +
            " = ? WHERE username = ?";
    public static final String DELETE_USER_BY_USERNAME = "DELETE FROM users WHERE username = ?";
    public static final String IS_USER_LOCKED = "SELECT is_locked FROM users WHERE username = ?";
    public static final String UPDATE_USER_LOCK_STATUS = "UPDATE users SET is_locked = ? WHERE username = ?";
}
