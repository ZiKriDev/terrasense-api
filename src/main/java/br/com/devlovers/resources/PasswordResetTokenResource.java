package br.com.devlovers.resources;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.devlovers.domain.password.dto.ResetPasswordDTO;
import br.com.devlovers.domain.password.dto.ResetPasswordRequestDTO;
import br.com.devlovers.domain.password.dto.UpdatePasswordDTO;
import br.com.devlovers.resources.exceptions.StandardError;
import br.com.devlovers.services.PasswordResetTokenService;
import br.com.devlovers.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(value = "/api/password")
public class PasswordResetTokenResource {

    @Autowired
    private PasswordResetTokenService service;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping(value = "/request-reset")
    @Operation(summary = "Envia e-mail de recuperação de senha")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "E-mail de recuperação de senha enviado com sucesso", content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "400", description = "Erro de validação", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardError.class))),
        @ApiResponse(responseCode = "500", description = "Erro ao enviar e-mail de recuperação de senha", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardError.class)))
    })
    public Mono<ResponseEntity<Void>> requestPasswordReset(@RequestBody @Valid ResetPasswordRequestDTO request) {
        return userService.findByLogin(request.email())
                .flatMap(user -> service.createPasswordTokenForUser(user))
                .then(Mono.just(ResponseEntity.ok().<Void>build()));
    }

    @PostMapping(value = "/reset")
    @Operation(summary = "Realiza o reset da senha")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Senha resetada com sucesso", content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "400", description = "Erro de validação", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardError.class))),
        @ApiResponse(responseCode = "404", description = "Usuário não encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardError.class)))
    })
    public Mono<ResponseEntity<Void>> resetPassword(@RequestBody @Valid ResetPasswordDTO request) {
        return service.findPasswordResetTokenByToken(request.token())
                .flatMap(resetToken -> userService.findByLogin(resetToken.getLogin())
                        .flatMap(user -> {
                            user.setPassword(passwordEncoder.encode(request.newPassword()));
                            UpdatePasswordDTO data = new UpdatePasswordDTO(user);
                            return userService.update(user.getUsername(), data);
                        })
                )
                .then(Mono.just(ResponseEntity.ok().<Void>build()));
    }
}
