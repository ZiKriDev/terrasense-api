package br.com.devlovers.resources;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.devlovers.resources.exceptions.StandardError;
import br.com.devlovers.services.ReportService;
import br.com.devlovers.services.report.dto.ReportRegisterDTO;
import br.com.devlovers.services.report.dto.ReportResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(value = "/api/reports")
public class ReportResource {

    @Autowired
    private ReportService reportService;

    @PostMapping(value = "/period")
    @SecurityRequirement(name = "bearer-key")
    @Operation(summary = "Gera relatórios a partir de um período de tempo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Relatórios gerados com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ReportResponseDTO.class, name = "ReportResponse"))),
            @ApiResponse(responseCode = "400", description = "Erro de validação", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardError.class))),
            @ApiResponse(responseCode = "401", description = "Token expirado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardError.class))),
            @ApiResponse(responseCode = "500", description = "Falha ao gerar relatório", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardError.class)))
    })
    public Mono<ResponseEntity<ReportResponseDTO>> generatePeriodicReports(@Valid @RequestBody ReportRegisterDTO data) {
        return reportService.getAllReports(
                data.startDate(),
                data.endDate(),
                data.type(),
                data.branch(),
                data.signatureName(),
                data.userId())
                .map(ResponseEntity::ok);
    }
}
