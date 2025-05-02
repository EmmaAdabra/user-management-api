package com.adb.usermanagementapi.repository;

import com.adb.usermanagementapi.config.TestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringJUnitConfig(TestConfig.class)
public class LoginAttemptsRepositoryTest {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private LoginAttemptsRepository loginAttemptsRepository;
    @Autowired
    private UserRepository userRepository;

    void setUp(){
        jdbcTemplate.update("DELETE FROM login_attempts");
        jdbcTemplate.update("DELETE FROM users");
    }

    @Test
    void countFailedAttemptsInLastTwoMinutes_noAttempts_returnsZero(){
        String username = "testuser";
        userRepository.save(username, "testuser@example.com", "hashedpassword");
        Long userId = userRepository.findIdByUsername(username);

        int count = loginAttemptsRepository.countFailedAttemptsInLastTwoMinutes(userId);
        assertEquals(0, count, "No login attempts should return zero");
    }
}
