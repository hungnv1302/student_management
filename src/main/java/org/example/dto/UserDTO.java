package org.example.dto;

public record UserDTO(
        String username,   // PK
        String password,
        String role,
        String state
) {}
