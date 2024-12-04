package br.com.devlovers.resources;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.devlovers.domain.user.dto.UserResponseAuthDTO;
import br.com.devlovers.resources.exceptions.StandardError;
import br.com.devlovers.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(value = "/api/users")
public class UserResource {

    @Autowired
    private UserService service;

    @GetMapping(value = "/{id}")
    @SecurityRequirement(name = "bearer-key")
    @Operation(summary = "Busca um usuário a partir de seu id")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Usuário obtido com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponseAuthDTO.class, name = "UserResponseToAuth"))),
        @ApiResponse(responseCode = "401", description = "Token expirado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardError.class))),
        @ApiResponse(responseCode = "404", description = "Usuário não encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardError.class)))
    })
    public Mono<ResponseEntity<UserResponseAuthDTO>> findById(@PathVariable UUID id) {
        return service.findById(id)
                .map(userById -> {
                    UserResponseAuthDTO dto = new UserResponseAuthDTO(userById);
                    return ResponseEntity.ok(dto);
                });
    }
}
