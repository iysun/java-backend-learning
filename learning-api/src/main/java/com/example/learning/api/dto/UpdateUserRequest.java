package com.example.learning.api.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class UpdateUserRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private String username;
    private String email;
    private Integer age;
}
