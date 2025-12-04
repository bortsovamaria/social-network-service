package com.otus.highload.security;


import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthRequest {

    @NotBlank(message = "Email обязателен")
    private String email;
    
    @NotBlank(message = "Пароль обязателен")
    private String password;

}