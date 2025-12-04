package com.otus.highload.service;


import com.otus.highload.config.PasswordEncoder;
import com.otus.highload.security.AuthResponse;
import com.otus.highload.model.User;
import com.otus.highload.model.UserResponse;
import com.otus.highload.security.JwtTokenProvider;
import com.otus.highload.security.RegisterResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;

    private final PasswordEncoder passwordEncoder;

    private final JwtTokenProvider jwtTokenProvider;

    public AuthResponse authenticate(String email, String password) {
        Optional<User> user = userService.findByEmail(email);

        if (user.isPresent() && passwordEncoder.matches(password, user.get().getPassword())) {
            String token = jwtTokenProvider.generateToken(email);
            return new AuthResponse(token, user.get().getId());
        }

        throw new RuntimeException("Invalid login or password");
    }

    public RegisterResponse register(User user) {
        UserResponse createdUser = userService.createUser(user);
        return new RegisterResponse(createdUser.getEmail());
    }
}