package com.adb.usermanagementapi.model.dto;

import java.time.LocalDateTime;

public record LoginAttempt(
        Long id,
        Long userId,
        LocalDateTime attemptTime,
        boolean success
) {}
