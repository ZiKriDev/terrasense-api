package br.com.devlovers.services.report.model;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;

import br.com.devlovers.domain.device.Device;
import br.com.devlovers.domain.device.enums.Branch;
import br.com.devlovers.domain.reading.Reading;
import br.com.devlovers.domain.reading.enums.ReadingType;
import br.com.devlovers.services.exceptions.ReportGenerationException;
import br.com.devlovers.services.report.util.ChartsCreator;
import br.com.devlovers.services.report.util.HeaderFiller;
import br.com.devlovers.services.report.util.ImageInserter;
import br.com.devlovers.services.report.util.InfoParagraphInserter;
import br.com.devlovers.services.report.util.MetricsFormCreator;
import br.com.devlovers.services.report.util.MetricsFormFiller;
import lombok.Getter;

@Getter
public class Template implements Document {

    private PDDocument document;
    private ChartsCreator chartsCreator;

    public Template(String templatePath) {
        this.chartsCreator = new ChartsCreator();
        try {
            URL resource = getClass().getClassLoader().getResource(templatePath);
            this.document = Loader.loadPDF(new File(resource.getFile()));
        } catch (IOException e) {
            throw new ReportGenerationException("Falha ao carregar template");
        }
    }

    @Override
    public float createMetricsForm(int numberOfDays) {
        MetricsFormCreator metricsFormCreator = new MetricsFormCreator();
        return metricsFormCreator.create(document, numberOfDays);
    }

    @Override
    public void fillHeader(Branch branch, LocalDate startDate, LocalDate endDate, Device device, ReadingType type) {
        HeaderFiller headerFiller = new HeaderFiller();
        headerFiller.fill(document, branch, startDate, endDate, device, type);
    }

    @Override
    public void fillMetricsForm(LocalDate startDate, LocalDate endDate, int numberOfDays, List<Reading> readings,
            ReadingType type) {
        MetricsFormFiller metricsFormFiller = new MetricsFormFiller();
        metricsFormFiller.fill(document, startDate, endDate, numberOfDays, type, readings);
    }

    @Override
    public float insertChartsAndSignature(ReadingType type, List<Reading> readings, float currentYPosition, String encodedSignature) {
        File frequencyChartFile = chartsCreator.createFrequencyChart(readings, type);
        File trendChartFile = chartsCreator.createTrendChart(readings, type);

        currentYPosition -= 95f;

        ImageInserter imageInserter = new ImageInserter();
        imageInserter.insertImage(document, frequencyChartFile, 70f, currentYPosition, 200f, 100f);
        imageInserter.insertImage(document, trendChartFile, 325f, currentYPosition, 200f, 100f);

        File signatureFile = getSignatureFile(encodedSignature);

        imageInserter.insertImage(document, signatureFile, 70f, 80f, 200f, 65f);

        return currentYPosition;
    }

    @Override
    public void insertInfoParagraph(Device device, LocalDate startDate, LocalDate endDate, ReadingType readingType,
            float currentYPosition, List<Reading> readings) {
        currentYPosition -= 25f;
        
        InfoParagraphInserter inserter = new InfoParagraphInserter();
        inserter.insertParagraph(document, startDate, endDate, 25f, currentYPosition, 550f, device, readingType, readings);
    }

    private File getSignatureFile(String encodedSignature) {
        String encodedSignatureWithoutHeader = encodedSignature.split(",")[1];

        byte[] decodedBytes = Base64.getDecoder().decode(encodedSignatureWithoutHeader);

        try {
            Path path = Files.createTempFile("temp-" + UUID.randomUUID() + "-chart", ".png");
            File signature = path.toFile();

            try (FileOutputStream fos = new FileOutputStream(signature)) {
                fos.write(decodedBytes);
            }

            return signature;
        } catch (IOException e) {
            throw new ReportGenerationException("Falha ao obter a assinatura como imagem");
        }
    }
}