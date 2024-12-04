package br.com.devlovers.services.report.util;

import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

import br.com.devlovers.services.exceptions.ReportGenerationException;

public class MetricsFormCreator {

    private static final float INITIAL_Y_POSITION = 575.8f;
    private static final float ROW_HEIGHT = 17.6f;
    private static final float LINE_WIDTH = 0.7f;

    private static final float[][] CELL_DIMENSIONS = {
            { 47.1f, 39.2f }, { 86.1f, 39.9f }, { 126f, 45f }, { 171f, 46f },
            { 217.1f, 39.8f }, { 256.9f, 42.5f }, { 299.6f, 44.7f }, { 344.5f, 42.4f },
            { 387.1f, 42.4f }, { 429.7f, 136.7f }
    };

    public float create(PDDocument document, int numberOfRows) {
        return drawRows(document, numberOfRows);
    }

    private float drawRows(PDDocument document, int numberOfRows) {
        float currentYPosition = INITIAL_Y_POSITION;

        for (int i = 0; i < numberOfRows; i++) {
            drawRow(document, currentYPosition);
            currentYPosition -= ROW_HEIGHT;
        }

        return currentYPosition;
    }

    private void drawRow(PDDocument document, float yPosition) {
        for (float[] cell : CELL_DIMENSIONS) {
            float xPosition = cell[0];
            float cellWidth = cell[1];
            drawCell(document, xPosition, yPosition, cellWidth, ROW_HEIGHT);
        }
    }

    private void drawCell(PDDocument document, float x, float y, float width, float height) {
        PDPage page = getOrCreateFirstPage(document);

        try (PDPageContentStream contentStream = new PDPageContentStream(
                document, page, PDPageContentStream.AppendMode.APPEND, true)) {
            contentStream.setStrokingColor(0, 0, 0);
            contentStream.setLineWidth(LINE_WIDTH);
            contentStream.addRect(x, y, width, height);
            contentStream.stroke();
        } catch (IOException e) {
            throw new ReportGenerationException("Falha ao desenhar célula da tabela de métricas no relatório");
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
}
