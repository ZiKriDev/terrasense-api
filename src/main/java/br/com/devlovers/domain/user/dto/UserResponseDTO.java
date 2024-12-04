package br.com.devlovers.domain.user.dto;

import java.util.UUID;

import br.com.devlovers.domain.user.User;
import br.com.devlovers.domain.user.enums.Role;

public record UserResponseDTO(

    UUID id,
    String login,
    Role role
) {

    public UserResponseDTO(User user) {
        this(user.getId(), user.getUsername(), user.getRole());
    }
}
