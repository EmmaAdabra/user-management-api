package com.adb.usermanagementapi.model.login;

import java.time.LocalDateTime;

public record LoginAttempt(
        Long id,
        Long userId,
        LocalDateTime attemptTime,
        boolean success
) {}
