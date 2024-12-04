package br.com.devlovers.domain.user.dto;

public record LoginResponseDTO(String token, UserResponseAuthDTO data) {

}
