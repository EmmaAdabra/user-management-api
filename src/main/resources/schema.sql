DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS login_attempts;


CREATE TABLE users (
   id INTEGER PRIMARY KEY AUTOINCREMENT,
   username TEXT NOT NULL UNIQUE,
   email TEXT NOT NULL UNIQUE,
   password_hash TEXT NOT NULL,
   created_at TEXT DEFAULT CURRENT_TIMESTAMP,
   is_locked INTEGER DEFAULT 0
);

CREATE TABLE login_attempts (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    attempt_time TEXT DEFAULT CURRENT_TIMESTAMP,
    success INTEGER NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_login_attempts_user_id_attempt_time_success
    ON login_attempts(user_id, attempt_time, success);