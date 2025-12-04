package com.otus.highload.service;

import com.otus.highload.config.PasswordEncoder;
import com.otus.highload.exception.EntityNotFoundException;
import com.otus.highload.exception.InvalidDataException;
import com.otus.highload.mapper.UserMapper;
import com.otus.highload.model.User;
import com.otus.highload.model.UserResponse;
import com.otus.highload.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserResponse createUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException(String.format("User with current email exists. Email: %s", user.getEmail()));
        }
        user.setId(String.valueOf(UUID.randomUUID()));
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setCreatedAt(LocalDateTime.now());
        User saved = userRepository.save(user);
        return userMapper.toResponse(saved);
    }

    public UserResponse getById(String id) {
        return userMapper.toResponse(userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(String.format("User with id = %s not found", id))));
    }

    public List<UserResponse> getByFirstNameAndLastName(String firstName, String lastName) {
        if (firstName == null || firstName.trim().isEmpty() ||
                lastName == null || lastName.trim().isEmpty()) {
            throw new InvalidDataException("First name or second name can't be null");
        }
        return userMapper.toResponse(userRepository.findByFirstNameAndLastNameIgnoreCase(firstName, lastName)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Users with firstName = %s and lastName = %s not found", firstName, lastName))));
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}
