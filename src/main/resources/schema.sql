CREATE TABLE IF NOT EXISTS users (
                       id UUID PRIMARY KEY,
                       firstName VARCHAR(255) NOT NULL,
                       secondName VARCHAR(255) NOT NULL,
                       birthdate DATE NOT NULL,
                       biography TEXT,
                       city VARCHAR(255),
                       passwordHash VARCHAR(255) NOT NULL
);