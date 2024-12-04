package br.com.devlovers.resources;

import java.net.URI;
import java.time.Instant;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import br.com.devlovers.domain.device.Device;
import br.com.devlovers.domain.device.Device.DeviceKey;
import br.com.devlovers.domain.device.dto.DeviceRegisterDTO;
import br.com.devlovers.domain.device.dto.DeviceResponseDTO;
import br.com.devlovers.domain.device.dto.DeviceUpdateDTO;
import br.com.devlovers.domain.device.enums.Branch;
import br.com.devlovers.domain.device.enums.Function;
import br.com.devlovers.domain.reading.dto.ReadingRegisterDTO;
import br.com.devlovers.domain.reading.dto.ReadingResponseDTO;
import br.com.devlovers.domain.reading.enums.ReadingType;
import br.com.devlovers.infra.security.KeyGeneratorService;
import br.com.devlovers.resources.exceptions.StandardError;
import br.com.devlovers.services.DeviceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(value = "/api/devices")
public class DeviceDataResource {

    @Autowired
    private DeviceService service;

    @Autowired
    private KeyGeneratorService keyGeneratorService;

    @PostMapping
    @SecurityRequirement(name = "bearer-key")
    @Operation(summary = "Insere um novo dispositivo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Dispositivo criado com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = DeviceResponseDTO.class, name = "DeviceResponse"))),
            @ApiResponse(responseCode = "400", description = "Erro de validação", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardError.class))),
            @ApiResponse(responseCode = "401", description = "Token expirado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardError.class))),
            @ApiResponse(responseCode = "409", description = "Dispositivo já existente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardError.class)))
    })
    public Mono<ResponseEntity<DeviceResponseDTO>> insertDevice(@Valid @RequestBody DeviceRegisterDTO data) {

        DeviceKey key = new Device.DeviceKey(data.name(), UUID.randomUUID());
        Device device = new Device(key, Branch.valueOf(data.branch()), Function.valueOf(data.function()),
                data.equipment(), data.patrimony(), data.tag(), data.sector(), data.minWorkingTemp(),
                data.maxWorkingTemp(), data.minWorkingHumidity(), data.maxWorkingHumidity(), true, data.ip(),
                keyGeneratorService.generate(32), data.deviceType(), data.sensor(), data.ownerId());

        return this.service.insertDevice(device)
                .map(savedDevice -> {
                    URI uri = UriComponentsBuilder.fromPath("/api/devices/{id}")
                            .buildAndExpand(savedDevice.getId())
                            .toUri();

                    DeviceResponseDTO response = new DeviceResponseDTO(savedDevice);
                    return ResponseEntity.created(uri).body(response);
                });
    }

    @PostMapping(value = "/readings")
    @SecurityRequirement(name = "api-key-scheme")
    @Operation(summary = "Insere uma nova leitura")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Leitura criada com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ReadingResponseDTO.class, name = "ReadResponse"))),
            @ApiResponse(responseCode = "400", description = "Erro de validação", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardError.class))),
            @ApiResponse(responseCode = "401", description = "Chave de API inválida", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardError.class)))
    })
    public Mono<ResponseEntity<ReadingResponseDTO>> insertReading(
            @Valid @RequestBody ReadingRegisterDTO data,
            @RequestHeader("X-API-KEY") String apiKey) {

        return service.findDeviceByApiKey(apiKey)
                .flatMap(device -> service.insertReading(apiKey, data.type(), data.value()))
                .map(reading -> {
                    ReadingResponseDTO response = new ReadingResponseDTO(reading);
                    return ResponseEntity.status(HttpStatus.CREATED).body(response);
                });
    }

    @GetMapping(value = "/{deviceId}/readings")
    @SecurityRequirement(name = "bearer-key")
    @Operation(summary = "Busca leituras a partir de um dispositivo, tipo de leitura e período de tempo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Leituras obtidas com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ReadingResponseDTO.class, name = "ReadResponse"))),
            @ApiResponse(responseCode = "401", description = "Token expirado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardError.class))),
            @ApiResponse(responseCode = "404", description = "Dispositivo não encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardError.class)))
    })
    public ResponseEntity<Flux<ReadingResponseDTO>> findReadingsByDevice(@PathVariable UUID deviceId,
            @RequestParam ReadingType type,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant end) {
        Flux<ReadingResponseDTO> readings = service.findReadings(deviceId, type, start, end)
                .map(reading -> new ReadingResponseDTO(reading));

        return ResponseEntity.ok(readings);
    }

    @GetMapping(value = "/{id}")
    @SecurityRequirement(name = "bearer-key")
    @Operation(summary = "Busca um dispositivo a partir de seu id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dispositivo obtido com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = DeviceResponseDTO.class, name = "DeviceResponse"))),
            @ApiResponse(responseCode = "401", description = "Token expirado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardError.class))),
            @ApiResponse(responseCode = "404", description = "Dispositivo não encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardError.class)))
    })
    public Mono<ResponseEntity<DeviceResponseDTO>> findDeviceById(@PathVariable UUID id) {
        return service.findDeviceById(id)
                .map(deviceById -> {
                    DeviceResponseDTO response = new DeviceResponseDTO(deviceById);
                    return ResponseEntity.ok(response);
                });
    }

    @GetMapping(value = "/user/{userId}")
    @SecurityRequirement(name = "bearer-key")
    @Operation(summary = "Busca dispositivos a partir do id do usuário")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dispositivos obtidos com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = DeviceResponseDTO.class, name = "DeviceResponse"))),
            @ApiResponse(responseCode = "401", description = "Token expirado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardError.class))),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardError.class)))
    })
    public Flux<DeviceResponseDTO> findDeviceByUserId(@PathVariable UUID userId) {
        return service.findDevicesByUserId(userId)
                .map(deviceByUserId -> new DeviceResponseDTO(deviceByUserId));
    }

    @GetMapping(value = "/restart/{id}")
    @SecurityRequirement(name = "bearer-key")
    @Operation(summary = "Reinicia o dispositivo a partir de seu id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dispositivo reiniciado com sucesso", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = "Token expirado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardError.class))),
            @ApiResponse(responseCode = "404", description = "Dispositivo não encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardError.class)))
    })
    public Mono<ResponseEntity<Void>> restartDevice(@PathVariable UUID id) {
        return service.restartDevice(id)
                .then(Mono.just(ResponseEntity.ok().<Void>build()));
    }

    @GetMapping(value = "/read/{id}")
    @SecurityRequirement(name = "bearer-key")
    @Operation(summary = "Força uma leitura a partir do id do dispositivo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Leitura feita com sucesso", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = "Token expirado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardError.class))),
            @ApiResponse(responseCode = "404", description = "Dispositivo não encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardError.class)))
    })
    public Mono<ResponseEntity<Void>> forceRead(@PathVariable UUID id) {
        return service.forceRead(id)
                .then(Mono.just(ResponseEntity.ok().<Void>build()));
    }

    @PutMapping(value = "/{id}")
    @SecurityRequirement(name = "bearer-key")
    @Operation(summary = "Atualiza dados do dispositivo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dados atualizados com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = DeviceResponseDTO.class, name = "DeviceResponse"))),
            @ApiResponse(responseCode = "400", description = "Erro de validação", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardError.class))),
            @ApiResponse(responseCode = "401", description = "Token expirado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardError.class))),
            @ApiResponse(responseCode = "404", description = "Dispositivo não encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardError.class)))
    })
    public Mono<ResponseEntity<DeviceResponseDTO>> updateDevice(@PathVariable UUID id,
            @RequestBody @Valid DeviceUpdateDTO data) {
        return service.updateDevice(id, data)
                .map(updatedDevice -> ResponseEntity.ok(new DeviceResponseDTO(updatedDevice)));
    }

    @DeleteMapping(value = "/{id}")
    @SecurityRequirement(name = "bearer-key")
    @Operation(summary = "Remove o registro de um dispositivo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Registro removido com sucesso", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = "Token expirado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardError.class))),
            @ApiResponse(responseCode = "404", description = "Dispositivo não encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardError.class)))
    })
    public Mono<ResponseEntity<Void>> deleteDevice(@PathVariable UUID id) {
        return service.deleteDevice(id)
                .then(Mono.just(ResponseEntity.noContent().build()));
    }
}