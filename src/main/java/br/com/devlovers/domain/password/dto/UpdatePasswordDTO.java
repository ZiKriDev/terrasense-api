package br.com.devlovers.domain.password.dto;

import br.com.devlovers.domain.user.User;

public record UpdatePasswordDTO(String password) {

    public UpdatePasswordDTO(User user) {
        this(user.getPassword());
    }
}
