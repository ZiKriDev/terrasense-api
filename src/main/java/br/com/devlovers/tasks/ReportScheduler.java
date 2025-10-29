package br.com.devlovers.tasks;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import br.com.devlovers.domain.device.enums.Branch;
import br.com.devlovers.domain.reading.enums.ReadingType;
import br.com.devlovers.services.ReportService;
import br.com.devlovers.services.report.dto.ReportDTO;
import br.com.devlovers.services.report.dto.ReportResponseDTO;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class ReportScheduler {

    private final ReportService reportService;
    private final WebClient webClient;

    @Value("${report.signatureName}")
    private String signatureName;

    @Value("${report.userId}")
    private UUID userId;

    @Value("${report.branch:MAIN}")
    private Branch branch;

    @Value("${report.receiver.token:}")
    private String receiverToken;

    @Value("${report.receiver.url}")
    private String receiverUrl;

    public ReportScheduler(ReportService reportService, WebClient.Builder webClientBuilder) {
        this.reportService = reportService;
        this.webClient = webClientBuilder.build();
    }

    @Scheduled(cron = "0 30 0 * * *", zone = "America/Sao_Paulo")
    public void generateAndSendDailyReports() {
        LocalDate yesterday = LocalDate.now().minusDays(1);

        log.info("Iniciando geração e envio de relatórios do dia {}", yesterday);

        Mono<ReportResponseDTO> temperatureReports = reportService.getAllReports(
                yesterday, yesterday, ReadingType.TEMPERATURE, branch, signatureName, userId);

        Mono<ReportResponseDTO> humidityReports = reportService.getAllReports(
                yesterday, yesterday, ReadingType.HUMIDITY, branch, signatureName, userId);

        Mono.zip(temperatureReports, humidityReports)
                .flatMap(tuple -> {
                    List<ReportDTO> reports = new ArrayList<>(tuple.getT1().reports());
                    reports.addAll(tuple.getT2().reports());
                    ReportResponseDTO combined = new ReportResponseDTO(reports);

                    log.info("Relatórios combinados gerados: {}", reports.size());
                    return sendReportsToReceiver(combined);
                })
                .doOnSuccess(response -> log.info("✅ Relatórios enviados com sucesso para {}", receiverUrl))
                .doOnError(error -> log.error("❌ Erro ao gerar ou enviar relatórios: {}", error.getMessage(), error))
                .subscribe();
    }

    private Mono<Void> sendReportsToReceiver(ReportResponseDTO reportResponse) {
        return webClient.post()
                .uri(receiverUrl)
                .header("X-API-KEY", receiverToken)
                .bodyValue(reportResponse)
                .retrieve()
                .bodyToMono(Void.class)
                .doOnSubscribe(s -> log.info("Enviando relatórios para {}", receiverUrl))
                .doOnError(e -> log.error("Falha ao enviar relatórios: {}", e.getMessage()));
    }
}