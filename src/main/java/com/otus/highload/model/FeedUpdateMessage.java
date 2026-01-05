package com.otus.highload.model;

import java.time.LocalDateTime;

public record FeedUpdateMessage(
        String postId,
        String authorId,
        LocalDateTime createdAt
) {

}