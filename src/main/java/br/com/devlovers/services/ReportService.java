package br.com.devlovers.services;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.devlovers.domain.device.Device;
import br.com.devlovers.domain.device.Device.DeviceKey;
import br.com.devlovers.domain.device.enums.Branch;
import br.com.devlovers.domain.reading.Reading;
import br.com.devlovers.domain.reading.enums.ReadingType;
import br.com.devlovers.services.exceptions.InvalidTimePeriodException;
import br.com.devlovers.services.exceptions.ReportGenerationException;
import br.com.devlovers.services.report.dto.ReportDTO;
import br.com.devlovers.services.report.dto.ReportResponseDTO;
import br.com.devlovers.services.report.factory.Factory;
import br.com.devlovers.services.report.model.Document;
import br.com.devlovers.services.report.model.Template;
import br.com.devlovers.util.DateRangePicker;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ReportService {

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private SignatureService signatureService;

    private final String TEMPLATE_PATH = "static/report/template/report-template.pdf";

    public Mono<ReportResponseDTO> getAllReports(LocalDate start, LocalDate end, ReadingType type, Branch branch,
            String signatureName, UUID userId) {

        long daysBetween = ChronoUnit.DAYS.between(start, end);

        if (daysBetween > 14) {
            throw new InvalidTimePeriodException("O período compreendido no relatório não deve ultrapassar 15 dias");
        }

        if (daysBetween < 0) {
            throw new InvalidTimePeriodException("O período compreendido no relatório não deve ser menor do que 1 dia");
        }

        return signatureService.getSignatureByNameAndUserId(signatureName, userId)
                .flatMap(signature -> deviceService.findDevicesByUserId(userId)
                        .map(deviceByUserId -> new Device(
                                deviceByUserId,
                                new DeviceKey(deviceByUserId.getName(), deviceByUserId.getKey().getId())))
                        .collectList()
                        .flatMapMany(devices -> Flux.fromIterable(devices))
                        .flatMap(device -> {
                            Instant startTime = DateRangePicker.getMoment(start);
                            Instant endTime = DateRangePicker.getMoment(end).plus(Duration.ofDays(1))
                                    .minus(Duration.ofMinutes(3));

                            return deviceService.findReadings(device.getId(), type, startTime, endTime)
                                    .collectList()
                                    .flatMap(readings -> {
                                        if (!readings.isEmpty()) {
                                            return Mono.fromCallable(() -> generateReport(start, end, type, branch,
                                                    userId, device, signature.encodedSignature(), readings));
                                        }
                                        return Mono.empty();
                                    });
                        })
                        .collectList()
                        .map(reports -> new ReportResponseDTO(reports)));
    }

    private ReportDTO generateReport(LocalDate start, LocalDate end, ReadingType type, Branch branch, UUID userId,
            Device device, String encodedSignature, List<Reading> readings) {
        Document document = Factory.createDoc(TEMPLATE_PATH);

        float currentYPosition = document.createMetricsForm(DateRangePicker.getNumberOfDays(start, end));
        document.fillHeader(branch, start, end, device, type);
        document.fillMetricsForm(start, end, DateRangePicker.getNumberOfDays(start, end), readings, type);

        currentYPosition = document.insertChartsAndSignature(type, readings, currentYPosition, encodedSignature);
        document.insertInfoParagraph(device, start, end, type, currentYPosition, readings);

        Template template = (Template) document;
        try {
            String encodedFile = "data:application/pdf;base64," + encode(template.getDocument());
            String fileName = generateFileName(device, start, end, type);
            return new ReportDTO(fileName, encodedFile);
        } finally {
            try {
                template.getDocument().close();
            } catch (IOException e) {
                throw new ReportGenerationException("Falha ao fechar relatório");
            }
        }
    }

    private String encode(PDDocument pdDocument) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            pdDocument.save(outputStream);
            byte[] pdfBytes = outputStream.toByteArray();
            return Base64.getEncoder().encodeToString(pdfBytes);
        } catch (IOException e) {
            throw new ReportGenerationException("Falha ao codificar arquivo do relatório");
        }
    }

    private String generateFileName(Device device, LocalDate start, LocalDate end, ReadingType type) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM");
        String startDate = start.format(formatter);
        String endDate = end.format(formatter);

        return "Relatório de "
                + type.fromString()
                + " "
                + startDate
                + " a "
                + endDate
                + " "
                + device.getSector()
                + " "
                + device.getTypeOfEquipment();
    }
}
