package org.example.dto;

public record UserDTO(
        String userId,
        String username,
        String password,
        String role,
        String state,
        String personId
) {}
