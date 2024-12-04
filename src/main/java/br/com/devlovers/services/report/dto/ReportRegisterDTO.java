package br.com.devlovers.services.report.dto;

import java.time.LocalDate;
import java.util.UUID;

import br.com.devlovers.domain.device.enums.Branch;
import br.com.devlovers.domain.reading.enums.ReadingType;

public record ReportRegisterDTO(

    UUID userId,
    LocalDate startDate,
    LocalDate endDate,
    ReadingType type,
    Branch branch,
    String signatureName
    
) {

}
