package com.otus.highload.controller;

import com.otus.highload.service.FriendshipService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v0/api/friendship")
public class FriendshipController {

    private final FriendshipService friendshipService;

    @PutMapping("/{userId}")
    public void addFriendToUser(@PathVariable String userId) {
        friendshipService.addFriendToUser(userId);
    }
}
