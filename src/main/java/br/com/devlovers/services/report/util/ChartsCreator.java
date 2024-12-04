package br.com.devlovers.services.report.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.jfree.chart.ChartColor;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.statistics.HistogramType;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import br.com.devlovers.domain.reading.Reading;
import br.com.devlovers.domain.reading.enums.ReadingType;
import br.com.devlovers.services.exceptions.ReportGenerationException;

public class ChartsCreator {

    public File createTrendChart(List<Reading> readings, ReadingType measurementType) {
        String mType = "";
        String unitOfMeasure = "";

        if (measurementType == ReadingType.TEMPERATURE) {
            mType = "Temperatura";
            unitOfMeasure = "°C";
        } else {
            mType = "Umidade";
            unitOfMeasure = "%";
        }
        XYSeries series = new XYSeries("Média de " + mType);

        for (Reading reading : readings) {
            long timestamp = reading.getTimestamp().toEpochMilli();
            Double tempValue = reading.getValue();

            series.add(timestamp, tempValue);
        }

        XYSeriesCollection dataset = new XYSeriesCollection(series);

        JFreeChart chart = ChartFactory.createXYLineChart(
                null,
                "Tempo",
                mType + " (" + unitOfMeasure + ")",
                dataset,
                PlotOrientation.VERTICAL,
                false,
                true,
                false);

        XYPlot plot = chart.getXYPlot();

        chart.setBackgroundPaint(null);
        plot.setBackgroundPaint(null);
        plot.setDomainGridlinePaint(ChartColor.BLACK);
        plot.setRangeGridlinePaint(ChartColor.BLACK);

        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesShapesVisible(0, false);
        renderer.setSeriesPaint(0, ChartColor.DARK_BLUE);
        plot.setRenderer(renderer);

        NumberAxis domain = (NumberAxis) plot.getDomainAxis();
        domain.setAutoRange(true);
        domain.setTickLabelsVisible(false);
        domain.setTickMarksVisible(false);
        domain.setLabelAngle(0);

        NumberAxis range = (NumberAxis) plot.getRangeAxis();
        range.setAutoRange(true);
        range.setAutoRangeIncludesZero(false);

        try {
            Path path = Files.createTempFile("temp-" + measurementType + "-Trend-chart", ".png");
            File temporaryFileChart = path.toFile();

            ChartUtils.saveChartAsPNG(temporaryFileChart, chart, 339, 169);

            return temporaryFileChart;
        } catch (IOException e) {
            throw new ReportGenerationException("Falha ao gerar arquivo do gráfico");
        }
    }

    public File createFrequencyChart(List<Reading> readings, ReadingType measurementType) {
        String mType = "";
        String unitOfMeasure = "";

        if (measurementType == ReadingType.TEMPERATURE) {
            mType = "Temperatura";
            unitOfMeasure = "°C";
        } else {
            mType = "Umidade";
            unitOfMeasure = "%";
        }

        HistogramDataset dataset = new HistogramDataset();
        dataset.setType(HistogramType.FREQUENCY);
        dataset.addSeries("Frequência de " + mType, readings.stream().mapToDouble(Reading::getValue).toArray(), 10);

        JFreeChart chart = ChartFactory.createHistogram(
                null,
                mType + " (" + unitOfMeasure + ")",
                "Frequência",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false);

        XYPlot plot = (XYPlot) chart.getPlot();

        chart.setBackgroundPaint(null);
        plot.setBackgroundPaint(null);
        plot.setDomainGridlinePaint(ChartColor.BLACK);
        plot.setRangeGridlinePaint(ChartColor.BLACK);

        XYBarRenderer renderer = new XYBarRenderer();
        renderer.setSeriesPaint(0, ChartColor.DARK_BLUE);
        plot.setRenderer(renderer);

        NumberAxis domain = (NumberAxis) plot.getDomainAxis();
        domain.setAutoRange(true);

        NumberAxis range = (NumberAxis) plot.getRangeAxis();
        range.setAutoRange(true);

        try {
            Path path = Files.createTempFile("temp-" + measurementType + "-Trend-chart", ".png");
            File temporaryFileChart = path.toFile();

            ChartUtils.saveChartAsPNG(temporaryFileChart, chart, 339, 169);

            return temporaryFileChart;
        } catch (IOException e) {
            throw new ReportGenerationException("Falha ao gerar arquivo do gráfico");
        }
    }
}
