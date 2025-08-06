CREATE TABLE users (
                       id INT PRIMARY KEY AUTO_INCREMENT,
                       username VARCHAR(255) NOT NULL UNIQUE,
                       email VARCHAR(255) NOT NULL UNIQUE,
                       password_hash VARCHAR(255) NOT NULL,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       is_locked BOOLEAN DEFAULT FALSE
);

CREATE TABLE login_attempts (
                                id INT PRIMARY KEY AUTO_INCREMENT,
                                user_id INT NOT NULL,
                                attempt_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                success BOOLEAN NOT NULL,
                                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_login_attempts_user_id_attempt_time_success
    ON login_attempts(user_id, attempt_time, success);