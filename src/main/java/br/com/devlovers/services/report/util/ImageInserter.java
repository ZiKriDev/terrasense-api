package br.com.devlovers.services.report.util;

import java.io.File;
import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import br.com.devlovers.services.exceptions.ReportGenerationException;

public class ImageInserter {

    public void insertImage(PDDocument document, File file, float x, float y, float width, float height) {
        try {
            PDImageXObject image = PDImageXObject.createFromFile(file.getAbsolutePath(), document);

            PDPage page = getOrCreateFirstPage(document);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true)) {
                contentStream.drawImage(image, x, y, width, height);
            }
        } catch (IOException e) {
            throw new ReportGenerationException("Falha ao inserir imagens no relatório");
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
