package com.otus.highload.controller;

import com.otus.highload.model.user.UserResponse;
import com.otus.highload.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v0/api/users")
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
    public UserResponse getUserById(@PathVariable String id) {
        return userService.getById(id);
    }

    @GetMapping("/search")
    public List<UserResponse> searchUsers(@RequestParam String firstName, @RequestParam String lastName) {
        return userService.getByFirstNameAndLastName(firstName, lastName);
    }
}