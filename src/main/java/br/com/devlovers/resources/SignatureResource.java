package br.com.devlovers.resources;

import java.net.URI;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import br.com.devlovers.domain.signature.dto.SignatureResponseDTO;
import br.com.devlovers.domain.signature.dto.SignatureUploadDTO;
import br.com.devlovers.resources.exceptions.StandardError;
import br.com.devlovers.services.SignatureService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(value = "/api/users/{userId}/signatures")
public class SignatureResource {

    @Autowired
    private SignatureService service;

    @PostMapping(value = "/upload")
    @SecurityRequirement(name = "bearer-key")
    @Operation(summary = "Cadastra uma nova assinatura")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Assinatura cadastrada com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SignatureResponseDTO.class, name = "SignatureResponse"))),
        @ApiResponse(responseCode = "400", description = "Erro de validação", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardError.class))),
        @ApiResponse(responseCode = "401", description = "Token expirado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardError.class))),
        @ApiResponse(responseCode = "404", description = "Usuário não encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardError.class)))
    })
    public Mono<ResponseEntity<SignatureResponseDTO>> uploadSignature(@PathVariable UUID userId,
            @RequestBody SignatureUploadDTO data) {
        return service.saveSignature(userId, data.encodedSignature(), data.fileName(), data.name())
                .map(savedSignature -> {
                    URI uri = UriComponentsBuilder.fromPath("/api/users/{userId}/signatures/{pictureId}")
                            .buildAndExpand(userId, savedSignature.getKey().getPictureId())
                            .toUri();

                    SignatureResponseDTO responseDTO = new SignatureResponseDTO(
                            savedSignature.getName(),
                            savedSignature.getFileName(),
                            data.encodedSignature());

                    return ResponseEntity.created(uri).body(responseDTO);
                });
    }

    @GetMapping
    @SecurityRequirement(name = "bearer-key")
    @Operation(summary = "Retorna todas as assinaturas de um usuário")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Assinatura cadastrada com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SignatureResponseDTO.class, name = "SignatureResponse"))),
        @ApiResponse(responseCode = "401", description = "Token expirado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardError.class))),
        @ApiResponse(responseCode = "404", description = "Usuário não encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardError.class)))
    })
    public Flux<SignatureResponseDTO> findAllByUserId(@PathVariable UUID userId) {
        return service.getSignaturesByUserId(userId);
    }
}
