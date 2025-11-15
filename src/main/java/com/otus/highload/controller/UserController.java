package com.otus.highload.controller;

import com.otus.highload.api.UserApi;
import com.otus.highload.exception.InvalidDataException;
import com.otus.highload.model.User;
import com.otus.highload.model.UserRegisterPost200Response;
import com.otus.highload.model.UserRegisterPostRequest;
import com.otus.highload.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class UserController implements UserApi {

    private final UserService userService;

    @Override
    public ResponseEntity<User> userGetIdGet(String id) {
        try {
            User user = userService.getById(id);
            return ResponseEntity.ok(user);
        } catch (com.otus.highload.exception.EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<UserRegisterPost200Response> userRegisterPost(UserRegisterPostRequest userRegisterPostRequest) {
        try {
            UserRegisterPost200Response response = userService.createUser(userRegisterPostRequest);
            return ResponseEntity.ok(response);
        } catch (InvalidDataException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<List<User>> userSearchGet(String firstName, String lastName) {
        try {
            List<User> users = userService.getByFirstNameAndLastName(firstName, lastName);
            return ResponseEntity.ok(users);
        } catch (InvalidDataException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}