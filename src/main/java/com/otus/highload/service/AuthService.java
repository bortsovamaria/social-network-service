package com.otus.highload.service;


import com.otus.highload.model.user.User;
import com.otus.highload.model.user.UserResponse;
import com.otus.highload.security.AuthResponse;
import com.otus.highload.security.JwtTokenProvider;
import com.otus.highload.security.RegisterResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        User user = userService.findByEmail(email);

        String storedPassword = user.getPassword();
        log.debug("Stored password hash: {}", storedPassword);

        if (storedPassword == null || storedPassword.isEmpty()) {
            log.error("Empty password in database for user: {}", email);
            throw new RuntimeException("Invalid login or password");
        }

        boolean matches = passwordEncoder.matches(password, storedPassword);
        log.debug("Password matches: {}", matches);

        if (matches) {
            String token = jwtTokenProvider.generateToken(user.getId());
            return new AuthResponse(token, user.getId());
        }

        throw new RuntimeException("Invalid login or password");
    }

    @Transactional
    public RegisterResponse register(User user) {
        UserResponse createdUser = userService.createUser(user);
        return new RegisterResponse(createdUser.getEmail());
    }
}