-- liquibase formatted sql

-- changeset author:1

CREATE TABLE IF NOT EXISTS friendship (
     user_id VARCHAR(255),
     friend_id VARCHAR(255),
     FOREIGN KEY (user_id) REFERENCES users(id),
     FOREIGN KEY (friend_id) REFERENCES users(id)
 );

CREATE INDEX IF NOT EXISTS idx_friendship_user_id ON friendship(user_id);
CREATE INDEX IF NOT EXISTS idx_friendship_user_friend ON friendship(user_id, friend_id);