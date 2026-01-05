package com.otus.highload.service;


import com.otus.highload.security.AuthResponse;
import com.otus.highload.model.user.User;
import com.otus.highload.model.user.UserResponse;
import com.otus.highload.security.JwtTokenProvider;
import com.otus.highload.security.RegisterResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;

    private final BCryptPasswordEncoder passwordEncoder;

    private final JwtTokenProvider jwtTokenProvider;

    @Transactional(readOnly = true)
    public AuthResponse authenticate(String email, String password) {
        log.info("Attempting authentication for email: {}", email);

        Optional<User> user = userService.findByEmail(email);

        if (user.isEmpty()) {
            log.warn("User not found: {}", email);
            throw new RuntimeException("Invalid login or password");
        }

        String storedPassword = user.get().getPassword();
        log.debug("Stored password hash: {}", storedPassword);

        if (storedPassword == null || storedPassword.isEmpty()) {
            log.error("Empty password in database for user: {}", email);
            throw new RuntimeException("Invalid login or password");
        }

        boolean matches = passwordEncoder.matches(password, storedPassword);
        log.debug("Password matches: {}", matches);

        if (matches) {
            String token = jwtTokenProvider.generateToken(email);
            return new AuthResponse(token, user.get().getId());
        }

        throw new RuntimeException("Invalid login or password");
    }

    @Transactional
    public RegisterResponse register(User user) {
        UserResponse createdUser = userService.createUser(user);
        return new RegisterResponse(createdUser.getEmail());
    }
}