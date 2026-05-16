package com.dto;

import lombok.Data;

import java.util.*;

@Data
public class LoginRequest {

    private UUID id;
    private String password;
}