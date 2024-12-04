package br.com.devlovers.services.report.util;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;

import br.com.devlovers.cache.FontCache;
import br.com.devlovers.domain.device.Device;
import br.com.devlovers.domain.device.enums.Branch;
import br.com.devlovers.domain.reading.enums.ReadingType;
import br.com.devlovers.services.exceptions.ReportGenerationException;

public class HeaderFiller {

    private static final float TITLE_X_POSITION = 75f;
    private static final float TITLE_Y_POSITION = 700f;
    private static final float PERIOD_X_POSITION = 410f;
    private static final float PERIOD_Y_POSITION = 770f;
    private static final float BRANCH_Y_POSITION = 752f;
    private static final float SECTOR_Y_POSITION = 734f;
    private static final float FUNCTION_X_POSITION = 95f;
    private static final float TYPE_X_POSITION = 245f;
    private static final float PATRIMONY_X_POSITION = 460f;
    private static final float DEVICE_Y_POSITION = 665f;
    private static final String TEMPERATURE_TITLE = "REGISTRO DE CONTROLE DIÁRIO DA TEMPERATURA DE CONSERVAÇÃO DE INSUMOS";
    private static final String HUMIDITY_TITLE = "REGISTRO DE CONTROLE DIÁRIO DA UMIDADE DE CONSERVAÇÃO DE INSUMOS";
    private static final String FONT_PATH_BOLD = "static/fonts/Arial-Bold.ttf";
    private static final String FONT_PATH_REGULAR = "static/fonts/Arial.ttf";
    private static final int FONT_SIZE = 11;

    public void fill(PDDocument document, Branch branch, LocalDate startDate, LocalDate endDate, Device device,
            ReadingType type) {
        insertPeriod(document, startDate, endDate);
        insertBranch(document, branch);
        insertSector(document, device);
        insertTitle(document, type);
        insertDeviceDetails(document, device);
    }

    private void insertPeriod(PDDocument document, LocalDate startDate, LocalDate endDate) {
        String period = formatPeriod(startDate, endDate);
        insertInCell(document, PERIOD_X_POSITION, PERIOD_Y_POSITION, period, false);
    }

    private void insertBranch(PDDocument document, Branch branch) {
        insertInCell(document, PERIOD_X_POSITION, BRANCH_Y_POSITION, branch.fromString(), false);
    }

    private void insertSector(PDDocument document, Device device) {
        insertInCell(document, PERIOD_X_POSITION, SECTOR_Y_POSITION, device.getSector(), false);
    }

    private void insertTitle(PDDocument document, ReadingType type) {
        String title = (type == ReadingType.TEMPERATURE) ? TEMPERATURE_TITLE : HUMIDITY_TITLE;
        insertInCell(document, TITLE_X_POSITION, TITLE_Y_POSITION, title, true);
    }

    private void insertDeviceDetails(PDDocument document, Device device) {
        insertInCell(document, FUNCTION_X_POSITION, DEVICE_Y_POSITION, device.getFunction().fromString(), false);
        insertInCell(document, TYPE_X_POSITION, DEVICE_Y_POSITION, device.getTypeOfEquipment(), false);
        insertInCell(document, PATRIMONY_X_POSITION, DEVICE_Y_POSITION, String.valueOf(device.getPatrimony()), false);
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
            throw new ReportGenerationException("Falha ao inserir dados no cabeçalho do relatório");
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

    private PDPage getOrCreateFirstPage(PDDocument document) {
        if (document.getNumberOfPages() > 0) {
            return document.getPage(0);
        } else {
            PDPage newPage = new PDPage(PDRectangle.A4);
            document.addPage(newPage);
            return newPage;
        }
    }

    private String formatPeriod(LocalDate startDate, LocalDate endDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");
        return String.format("%s a %s de %d",
                startDate.format(formatter),
                endDate.format(formatter),
                endDate.getYear());
    }
}
