CREATE TABLE IF NOT EXISTS users (
                       id VARCHAR(255),
                       firstName VARCHAR(255) NOT NULL,
                       secondName VARCHAR(255) NOT NULL,
                       birthdate DATE NOT NULL,
                       biography TEXT,
                       city VARCHAR(255),
                       passwordHash VARCHAR(255)
);

-- ALTER TABLE users ALTER COLUMN id SET DEFAULT gen_random_uuid();