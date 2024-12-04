package br.com.devlovers.services.report.util;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;

import br.com.devlovers.cache.FontCache;
import br.com.devlovers.domain.reading.Reading;
import br.com.devlovers.domain.reading.enums.ReadingType;
import br.com.devlovers.services.exceptions.ReportGenerationException;

public class MetricsFormFiller {

    private final AnalyticsCreator analyticsCreator;

    private static final float CELL_TITLE_X_POSITION = 225f;
    private static final float CELL_TITLE_Y_POSITION = 633f;
    private static final float CELL_ZERO_X_POSITION = 61f;
    private static final float CELL_Y = 580f;

    private static final float[] CELL_X_POSITIONS = {
            93f, 136f, 182f, 225f, 266f, 309f, 353f, 396f, 486f
    };

    private static final int FONT_SIZE = 10;
    private static final String FONT_PATH_BOLD = "static/fonts/Arial-Bold.ttf";
    private static final String FONT_PATH_REGULAR = "static/fonts/Arial.ttf";

    public MetricsFormFiller() {
        this.analyticsCreator = new AnalyticsCreator();
    }

    public void fill(
            PDDocument document,
            LocalDate startDate,
            LocalDate endDate,
            int numberOfDays,
            ReadingType type,
            List<Reading> readings) {

        addTitle(document, type);

        Map<Integer, Map<String, Double>> means = analyticsCreator.getMeans(readings);
        Map<Integer, Double> standardDeviations = analyticsCreator.getStandardDeviation(readings);
        Map<Integer, Double> minMeasures = analyticsCreator.getMin(readings);
        Map<Integer, Double> maxMeasures = analyticsCreator.getMax(readings);

        addDayData(document, startDate, numberOfDays);
        addNestedMetricData(document, means, startDate, numberOfDays);
        addMetricData(document, standardDeviations, startDate, numberOfDays, day -> CELL_X_POSITIONS[8]);
        addMetricData(document, minMeasures, startDate, numberOfDays, day -> CELL_X_POSITIONS[7]);
        addMetricData(document, maxMeasures, startDate, numberOfDays, day -> CELL_X_POSITIONS[6]);
    }

    private void addTitle(PDDocument document, ReadingType type) {
        String title = type == ReadingType.TEMPERATURE ? "TEMPERATURA (°C)" : "UMIDADE (%)";
        insertInCell(document, CELL_TITLE_X_POSITION, CELL_TITLE_Y_POSITION, title, true);
    }

    private void addDayData(PDDocument document, LocalDate startDate, int numberOfDays) {
        float decrementer = 0f;
        float incrementer = 0f;

        for (int i = 0; i < numberOfDays; i++) {
            insertInCell(document, CELL_ZERO_X_POSITION, CELL_Y - decrementer,
                    String.valueOf(startDate.getDayOfMonth() + i), false);
            decrementer += calculateDecrement(incrementer, i);
            incrementer = updateIncrementer(incrementer, i);
        }
    }

    private <T> void addMetricData(
            PDDocument document,
            Map<Integer, T> metrics,
            LocalDate startDate,
            int numberOfDays,
            MetricPositionCalculator positionCalculator) {

        float decrementer = 0f;
        float incrementer = 0f;
        int day = startDate.getDayOfMonth();

        for (int i = 0; i < numberOfDays; i++) {
            T metric = metrics.getOrDefault(day, null);

            if (metric != null) {
                insertInCell(document, positionCalculator.getXPosition(day), CELL_Y - decrementer, formatMetric(metric),
                        false);
            }

            decrementer += calculateDecrement(incrementer, i);
            incrementer = updateIncrementer(incrementer, i);
            day++;
        }
    }

    private void addNestedMetricData(
            PDDocument document,
            Map<Integer, Map<String, Double>> nestedMetrics,
            LocalDate startDate,
            int numberOfDays) {

        float decrementer = 0f;
        float incrementer = 0f;
        int day = startDate.getDayOfMonth();

        for (int i = 0; i < numberOfDays; i++) {
            Map<String, Double> metrics = nestedMetrics.getOrDefault(day, null);

            if (metrics != null) {
                for (Map.Entry<String, Double> entry : metrics.entrySet()) {
                    float xPos = getXPositionForPeriod(entry.getKey());
                    insertInCell(document, xPos, CELL_Y - decrementer, formatMetric(entry.getValue()), false);
                }
            }

            decrementer += calculateDecrement(incrementer, i);
            incrementer = updateIncrementer(incrementer, i);
            day++;
        }
    }

    private float calculateDecrement(float incrementer, int iteration) {
        return 17f + incrementer;
    }

    private float updateIncrementer(float incrementer, int iteration) {
        return iteration % 2 == 0 ? incrementer + 0.11f : incrementer;
    }

    private String formatMetric(Object metric) {
        return metric instanceof Double ? String.format("%.2f", (Double) metric) : metric.toString();
    }

    private float getXPositionForPeriod(String period) {
        return switch (period) {
            case "0-4h" -> CELL_X_POSITIONS[0];
            case "4-8h" -> CELL_X_POSITIONS[1];
            case "8-12h" -> CELL_X_POSITIONS[2];
            case "12-16h" -> CELL_X_POSITIONS[3];
            case "16-20h" -> CELL_X_POSITIONS[4];
            case "20-0h" -> CELL_X_POSITIONS[5];
            default -> -1f;
        };
    }

    private void insertInCell(PDDocument document, float xPos, float yPos, String value, boolean isBold) {
        PDFont font = loadFont(document, isBold);
        if (font == null)
            return;

        try (PDPageContentStream contentStream = new PDPageContentStream(document, getOrCreateFirstPage(document),
                PDPageContentStream.AppendMode.APPEND, true)) {
            contentStream.setFont(font, FONT_SIZE);
            contentStream.beginText();
            contentStream.newLineAtOffset(xPos, yPos);
            contentStream.showText(value);
            contentStream.endText();
        } catch (IOException e) {
            throw new ReportGenerationException("Falha ao inserir dados na tabela de métricas do relatório");
        }
    }

    private PDPage getOrCreateFirstPage(PDDocument document) {
        if (document.getNumberOfPages() > 0) {
            return document.getPage(0);
        } else {
            PDPage newPage = new PDPage(PDRectangle.A4);
            document.addPage(newPage);
            return newPage;
        }
    }

    private PDFont loadFont(PDDocument document, boolean isBold) {
        String fontPath = isBold ? FONT_PATH_BOLD : FONT_PATH_REGULAR;
        try {
            return FontCache.getFont(document, fontPath);
        } catch (IOException e) {
            throw new ReportGenerationException("Falha ao carregar arquivo de fonte");
        }
    }

    @FunctionalInterface
    private interface MetricPositionCalculator {
        float getXPosition(int day);
    }
}
