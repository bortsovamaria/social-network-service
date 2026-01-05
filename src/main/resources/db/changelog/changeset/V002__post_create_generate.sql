-- liquibase formatted sql

-- changeset author:1

CREATE TABLE IF NOT EXISTS posts (
     id VARCHAR(255) PRIMARY KEY,
     "text" TEXT NOT NULL,
     author_id VARCHAR(255) NOT NULL,
     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
 );

CREATE INDEX IF NOT EXISTS idx_posts_author_id ON posts(author_id);
CREATE INDEX IF NOT EXISTS idx_posts_created_at ON posts(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_posts_author_created ON posts(author_id, created_at DESC);