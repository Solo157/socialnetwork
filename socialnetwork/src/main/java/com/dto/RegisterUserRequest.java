package com.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class RegisterUserRequest {

    private String firstName;
    private String secondName;
    private LocalDate birthdate;
    private String biography;
    private String city;
    private String password;
}