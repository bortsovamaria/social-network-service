package com.otus.highload.service;

import com.otus.highload.exception.EntityNotFoundException;
import com.otus.highload.exception.InvalidDataException;
import com.otus.highload.mapper.UserMapper;
import com.otus.highload.model.User;
import com.otus.highload.model.UserRegisterPost200Response;
import com.otus.highload.model.UserRegisterPostRequest;
import com.otus.highload.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserRegisterPost200Response createUser(UserRegisterPostRequest userRequest) {
        User user = userMapper.toDomain(userRequest);
        user.setId(String.valueOf(UUID.randomUUID()));
        User saved = userRepository.save(user);
        return userMapper.toResponse(saved);
    }

    public User getById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(String.format("User with id = %s not found", id)));
    }

    public List<User> getByFirstNameAndLastName(String firstName, String lastName) {
        if (firstName == null || firstName.trim().isEmpty() ||
                lastName == null || lastName.trim().isEmpty()) {
            throw new InvalidDataException("First name or second name can't be null");
        }
        return userRepository.findByFirstNameAndLastNameIgnoreCase(firstName, lastName);
    }
}
