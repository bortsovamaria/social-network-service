package com.otus.highload.model.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {

    private String id;

    private String firstName;

    private String lastName;

    private String email;

    private LocalDateTime birthDate;

    private String biography;

    private String city;

    private String password;

    private LocalDateTime createdAt;

}