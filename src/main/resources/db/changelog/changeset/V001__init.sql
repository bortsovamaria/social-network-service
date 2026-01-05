-- liquibase formatted sql

-- changeset author:1

CREATE TABLE IF NOT EXISTS users (
     id VARCHAR(255) PRIMARY KEY,
     first_name VARCHAR(50) NOT NULL,
     last_name VARCHAR(50) NOT NULL,
     email VARCHAR(250) UNIQUE NOT NULL,
     birthdate TIMESTAMP,
     biography TEXT,
     city VARCHAR(100),
     password VARCHAR(255) NOT NULL,
     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
 );

CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE INDEX IF NOT EXISTS idx_gin_trgm_names ON users USING gin (
    first_name gin_trgm_ops,
    last_name gin_trgm_ops
);