package com.otus.highload.controller;


import com.otus.highload.model.User;
import com.otus.highload.security.AuthRequest;
import com.otus.highload.security.AuthResponse;
import com.otus.highload.security.RegisterResponse;
import com.otus.highload.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
@RequestMapping("/v0/api/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public RegisterResponse register(@Valid @RequestBody User user) {
        return authService.register(user);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody AuthRequest authRequest) {
        return authService.authenticate(authRequest.getEmail(), authRequest.getPassword());
    }
}