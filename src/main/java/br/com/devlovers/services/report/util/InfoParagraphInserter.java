package br.com.devlovers.services.report.util;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;

import br.com.devlovers.cache.FontCache;
import br.com.devlovers.domain.device.Device;
import br.com.devlovers.domain.reading.Reading;
import br.com.devlovers.domain.reading.enums.ReadingType;
import br.com.devlovers.services.exceptions.ReportGenerationException;
import lombok.Getter;

public class InfoParagraphInserter {

    private static final String FONT_PATH_BOLD = "static/fonts/Arial-Bold.ttf";
    private static final String FONT_PATH_REGULAR = "static/fonts/Arial.ttf";
    private static final int FONT_SIZE = 10;
    private static final float LINE_SPACING = 1.5f;
    private static final float BOTTOM_MARGIN = 50;

    private final AnalyticsCreator analyticsCreator;

    public InfoParagraphInserter() {
        this.analyticsCreator = new AnalyticsCreator();
    }

    public void insertParagraph(PDDocument document, LocalDate start, LocalDate end, float xPos, float yPos,
            float maxWidth, Device device, ReadingType type, List<Reading> readings) {
        ParagraphInfo paragraphInfo = buildParagraphInfo(device, type, readings, start, end);
        String paragraph = createParagraphText(paragraphInfo);

        PDFont font = loadFont(document, false);
        if (font == null)
            return;

        writeParagraph(document, xPos, yPos, maxWidth, font, paragraph);
    }

    private ParagraphInfo buildParagraphInfo(Device device, ReadingType type, List<Reading> readings, LocalDate start,
            LocalDate end) {
        String measurementType, unit, minWorking, maxWorking, timeInRange, note;

        if (type == ReadingType.TEMPERATURE) {
            measurementType = "temperatura";
            unit = "°C";
            timeInRange = analyticsCreator.getWorkingPeriodInCorrectTemperatureRange(device, readings);
            minWorking = formatValue(device.getMinWorkingTemp());
            maxWorking = formatValue(device.getMaxWorkingTemp());
            note = analyticsCreator.getNoteIfTemperatureNotWorkedInRange(device, readings);
        } else {
            measurementType = "umidade";
            unit = "%";
            timeInRange = analyticsCreator.getWorkingPeriodInCorrectHumidityRange(device, readings);
            minWorking = formatValue(device.getMinWorkingHumidity());
            maxWorking = formatValue(device.getMaxWorkingHumidity());
            note = analyticsCreator.getNoteIfHumidityNotWorkedInRange(device, readings);
        }

        return new ParagraphInfo(measurementType, unit, minWorking, maxWorking, timeInRange, note, device, start, end);
    }

    private String createParagraphText(ParagraphInfo info) {
        return String.format(
                "O controle de %s foi realizado utilizando um sensor SONOFF, de identificação %s, com coleta em tempo real em intervalos médios de 3 minutos. "
                        + "Conforme registro compreendido no período de %s, a %s de trabalho observada permaneceu no intervalo de %s a %s %s durante %s.%s",
                info.getMeasurementType(),
                info.getDevice().getTag(),
                formatPeriod(info.getStart(), info.getEnd()),
                info.getMeasurementType(),
                info.getMinWorking(),
                info.getMaxWorking(),
                info.getUnit(),
                info.getTimeInRange(),
                info.getNote());
    }

    private PDFont loadFont(PDDocument document, boolean isBold) {
        String fontPath = isBold ? FONT_PATH_BOLD : FONT_PATH_REGULAR;
        try {
            return FontCache.getFont(document, fontPath);
        } catch (IOException e) {
            throw new ReportGenerationException("Falha ao carregar arquivo de fonte");
        }
    }

    private void writeParagraph(PDDocument document, float xPos, float yPos, float maxWidth, PDFont font,
            String paragraph) {
        try (PDPageContentStream contentStream = new PDPageContentStream(document, getOrCreateFirstPage(document),
                PDPageContentStream.AppendMode.APPEND, true)) {

            contentStream.setFont(font, FONT_SIZE);
            contentStream.beginText();
            contentStream.newLineAtOffset(xPos, yPos);

            float currentY = yPos;
            for (String line : splitTextIntoLines(paragraph, font, FONT_SIZE, maxWidth)) {
                if (currentY <= BOTTOM_MARGIN) {
                    contentStream.endText();
                    contentStream.close();
                    contentStream.beginText();
                    currentY = addNewPageAndResetCursor(document, contentStream, xPos);
                }

                contentStream.showText(line);
                contentStream.newLineAtOffset(0, -FONT_SIZE * LINE_SPACING);
                currentY -= FONT_SIZE * LINE_SPACING;
            }

            contentStream.endText();

        } catch (IOException e) {
            throw new ReportGenerationException("Falha ao inserir parágrafo descritivo");
        }
    }

    private float addNewPageAndResetCursor(PDDocument document, PDPageContentStream contentStream, float xPos)
            throws IOException {
        PDPage newPage = new PDPage(PDRectangle.A4);
        document.addPage(newPage);
        float newY = newPage.getMediaBox().getHeight() - BOTTOM_MARGIN;
        contentStream.setFont(loadFont(document, false), FONT_SIZE);
        contentStream.newLineAtOffset(xPos, newY);
        return newY;
    }

    private PDPage getOrCreateFirstPage(PDDocument document) {
        if (document.getNumberOfPages() > 0) {
            return document.getPage(0);
        }
        PDPage newPage = new PDPage(PDRectangle.A4);
        document.addPage(newPage);
        return newPage;
    }

    private String formatPeriod(LocalDate startDate, LocalDate endDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");
        return String.format("%s a %s de %d", startDate.format(formatter), endDate.format(formatter),
                endDate.getYear());
    }

    private String formatValue(Double value) {
        return String.format("%.2f", value);
    }

    private String[] splitTextIntoLines(String text, PDFont font, int fontSize, float maxWidth) throws IOException {
        String[] words = text.split("\\s+");
        StringBuilder line = new StringBuilder();
        float spaceWidth = font.getStringWidth(" ") / 1000 * fontSize;
        float currentWidth = 0;

        List<String> lines = new ArrayList<>();
        for (String word : words) {
            float wordWidth = font.getStringWidth(word) / 1000 * fontSize;

            if (currentWidth + wordWidth > maxWidth) {
                lines.add(line.toString());
                line = new StringBuilder(word);
                currentWidth = wordWidth + spaceWidth;
            } else {
                if (line.length() > 0) {
                    line.append(" ");
                    currentWidth += spaceWidth;
                }
                line.append(word);
                currentWidth += wordWidth;
            }
        }

        if (line.length() > 0) {
            lines.add(line.toString());
        }

        return lines.toArray(new String[0]);
    }

    @Getter
    private static class ParagraphInfo {
        private final String measurementType;
        private final String unit;
        private final String minWorking;
        private final String maxWorking;
        private final String timeInRange;
        private final String note;
        private final Device device;
        private final LocalDate start;
        private final LocalDate end;

        public ParagraphInfo(String measurementType, String unit, String minWorking, String maxWorking,
                String timeInRange, String note, Device device, LocalDate start, LocalDate end) {
            this.measurementType = measurementType;
            this.unit = unit;
            this.minWorking = minWorking;
            this.maxWorking = maxWorking;
            this.timeInRange = timeInRange;
            this.note = note;
            this.device = device;
            this.start = start;
            this.end = end;
        }
    }
}
