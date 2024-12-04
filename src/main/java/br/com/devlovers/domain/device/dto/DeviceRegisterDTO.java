package br.com.devlovers.domain.device.dto;

import java.util.UUID;

import br.com.devlovers.domain.device.enums.DeviceType;
import br.com.devlovers.domain.device.enums.Sensor;
import br.com.devlovers.domain.device.validations.IP;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DeviceRegisterDTO(

    @NotBlank
    String name,

    @NotNull
    String branch,

    @NotNull
    String function,

    @NotBlank
    String equipment,

    @NotNull
    Long patrimony,
    
    @NotBlank
    String tag,

    @NotBlank
    String sector,

    @NotBlank
    @IP
    String ip,

    @NotNull
    DeviceType deviceType,

    @NotNull
    Sensor sensor,

    Double minWorkingTemp,
    Double maxWorkingTemp,
    Double minWorkingHumidity,
    Double maxWorkingHumidity,

    @NotNull
    UUID ownerId
) {

}
