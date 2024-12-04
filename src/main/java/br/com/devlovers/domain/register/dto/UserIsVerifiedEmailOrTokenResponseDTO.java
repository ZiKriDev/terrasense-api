package br.com.devlovers.domain.register.dto;

import br.com.devlovers.domain.register.UserRegisterToken;

public record UserIsVerifiedEmailOrTokenResponseDTO(

    Boolean isVerified
) {
    public UserIsVerifiedEmailOrTokenResponseDTO(UserRegisterToken token) {
        this(token.getIsVerified());
    }
}
