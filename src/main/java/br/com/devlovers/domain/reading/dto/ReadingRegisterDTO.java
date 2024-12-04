package br.com.devlovers.domain.reading.dto;

import br.com.devlovers.domain.reading.enums.ReadingType;
import jakarta.validation.constraints.NotNull;

public record ReadingRegisterDTO(

    @NotNull
    ReadingType type,
    
    @NotNull
    Double value
) {

}
