package br.com.devlovers.domain.user.dto;

import java.util.UUID;

import br.com.devlovers.domain.user.User;
import br.com.devlovers.domain.user.UserById;
import br.com.devlovers.domain.user.enums.Role;

public record UserResponseAuthDTO(
    
    UUID id,
    String login,
    Role role
)
{
    public UserResponseAuthDTO(User user) {
        this(user.getId(), user.getUsername(), user.getRole());
    }

    public UserResponseAuthDTO(UserById user) {
        this(user.getId(), user.getLogin(), user.getRole());
    }
}
