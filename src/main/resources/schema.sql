
CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(255) NOT NULL PRIMARY KEY,
    firstName VARCHAR(255) NOT NULL,
    secondName VARCHAR(255) NOT NULL,
    birthdate DATE NOT NULL,
    biography TEXT,
    city VARCHAR(255),
    passwordHash VARCHAR(255),
    friends TEXT
);

CREATE TABLE IF NOT EXISTS posts (
    id VARCHAR(255) NOT NULL PRIMARY KEY,
    text TEXT NOT NULL,
    author_id VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (author_id) REFERENCES users(id)
);
