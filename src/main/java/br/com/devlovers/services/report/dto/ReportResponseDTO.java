package br.com.devlovers.services.report.dto;

import java.util.List;

public record ReportResponseDTO(

    List<ReportDTO> reports

) {

}