package br.com.devlovers.services.report.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import br.com.devlovers.domain.device.Device;
import br.com.devlovers.domain.device.enums.Function;
import br.com.devlovers.domain.reading.Reading;

public class AnalyticsCreator {

    public Double getMean(List<Reading> readings) {
        if (!readings.isEmpty()) {
            Integer numberOfReadings = readings.size();
            Double sumOfReadings = 0.0;

            for (Reading reading : readings) {
                sumOfReadings += reading.getValue();
            }

            Double mean = sumOfReadings / numberOfReadings;

            return mean;
        }

        return 0.0;
    }

    public Map<Integer, Map<String, Double>> getMeans(List<Reading> readings) {
        readings.sort(Comparator.comparing(Reading::getTimestamp));

        // Leituras por período por dia
        Map<Integer, Map<String, List<Reading>>> readingsByPeriodByDay = readings.stream()
                .collect(Collectors.groupingBy(
                        reading -> getDay(reading.getTimestamp()),
                        Collectors.groupingBy(
                                reading -> getPeriod(reading.getTimestamp()),
                                Collectors.toList())));

        // Média de cada período por dia
        Map<Integer, Map<String, Double>> meanByPeriodByDay = new HashMap<>();

        for (Map.Entry<Integer, Map<String, List<Reading>>> dayEntry : readingsByPeriodByDay.entrySet()) {
            Integer day = dayEntry.getKey();
            Map<String, List<Reading>> periodMap = dayEntry.getValue();

            // Média por período
            Map<String, Double> meanByPeriod = new HashMap<>();

            for (Map.Entry<String, List<Reading>> periodEntry : periodMap.entrySet()) {
                String period = periodEntry.getKey();
                List<Reading> periodReadings = periodEntry.getValue();

                DescriptiveStatistics stats = new DescriptiveStatistics();
                periodReadings.forEach(reading -> stats.addValue(reading.getValue()));

                double mean = stats.getMean();

                meanByPeriod.put(period, mean);
            }

            meanByPeriodByDay.put(day, meanByPeriod);
        }

        return meanByPeriodByDay;
    }

    public Map<Integer, Double> getStandardDeviation(List<Reading> readings) {
        readings.sort(Comparator.comparing(Reading::getTimestamp));

        // Leituras por dia
        Map<Integer, List<Reading>> readingsByDay = readings.stream()
                .collect(Collectors.groupingBy(
                        reading -> getDay(reading.getTimestamp())));

        // Desvio padrão por dia
        Map<Integer, Double> standardDeviationByDay = new HashMap<>();

        for (Map.Entry<Integer, List<Reading>> dayEntry : readingsByDay.entrySet()) {
            Integer day = dayEntry.getKey();
            List<Reading> dayReadings = dayEntry.getValue();

            DescriptiveStatistics stats = new DescriptiveStatistics();
            dayReadings.forEach(reading -> stats.addValue(reading.getValue()));

            double standardDeviation = stats.getStandardDeviation();

            standardDeviationByDay.put(day, standardDeviation);
        }

        return standardDeviationByDay;
    }

    public Map<Integer, Double> getMin(List<Reading> readings) {
        readings.sort(Comparator.comparing(Reading::getTimestamp));

        // Leituras por dia
        Map<Integer, List<Reading>> readingsByDay = readings.stream()
                .collect(Collectors.groupingBy(
                        reading -> getDay(reading.getTimestamp())));

        // Valor mínimo de leitura por dia
        Map<Integer, Double> minByDay = new HashMap<>();

        for (Map.Entry<Integer, List<Reading>> dayEntry : readingsByDay.entrySet()) {
            Integer day = dayEntry.getKey();
            List<Reading> dayReadings = dayEntry.getValue();

            DescriptiveStatistics stats = new DescriptiveStatistics();
            dayReadings.forEach(reading -> stats.addValue(reading.getValue()));

            double min = stats.getMin();

            minByDay.put(day, min);
        }

        return minByDay;
    }

    public Map<Integer, Double> getMax(List<Reading> readings) {
        readings.sort(Comparator.comparing(Reading::getTimestamp));

        // Leituras por dia
        Map<Integer, List<Reading>> readingsByDay = readings.stream()
                .collect(Collectors.groupingBy(
                        reading -> getDay(reading.getTimestamp())));

        // Valor máximo de leitura por dia
        Map<Integer, Double> maxByDay = new HashMap<>();

        for (Map.Entry<Integer, List<Reading>> dayEntry : readingsByDay.entrySet()) {
            Integer day = dayEntry.getKey();
            List<Reading> dayReadings = dayEntry.getValue();

            DescriptiveStatistics stats = new DescriptiveStatistics();
            dayReadings.forEach(reading -> stats.addValue(reading.getValue()));

            double max = stats.getMax();

            maxByDay.put(day, max);
        }

        return maxByDay;
    }

    public String getWorkingPeriodInCorrectTemperatureRange(Device device, List<Reading> readings) {
        double minTemp = device.getMinWorkingTemp();
        double maxTemp = device.getMaxWorkingTemp();

        long countInRange = readings.stream()
                .filter(reading -> reading.getValue() >= minTemp && reading.getValue() <= maxTemp).count();

        long totalMinutesInRange = countInRange * 3;

        return formatTime(totalMinutesInRange);
    }

    public String getWorkingPeriodInCorrectHumidityRange(Device device, List<Reading> readings) {
        double minHumi = device.getMinWorkingHumidity();
        double maxHumi = device.getMaxWorkingHumidity();

        long countInRange = readings.stream()
                .filter(reading -> reading.getValue() >= minHumi && reading.getValue() <= maxHumi).count();

        long totalMinutesInRange = countInRange * 3;

        return formatTime(totalMinutesInRange);
    }

    public String getNoteIfTemperatureNotWorkedInRange(Device device, List<Reading> readings) {
        double minTemp = device.getMinWorkingTemp();
        double maxTemp = device.getMaxWorkingTemp();

        int consecutiveOutOfRangeCount = 0;

        for (Reading reading : readings) {
            if (reading.getValue() < minTemp || reading.getValue() > maxTemp) {
                consecutiveOutOfRangeCount++;
            } else {
                consecutiveOutOfRangeCount = 0;
            }

            if (consecutiveOutOfRangeCount == 7) {
                if (device.getFunction() == Function.EQUIPMENT) {
                    return " (Importante: verificar a vedação, evitar deixar o equipamento aberto por muito tempo)";
                }

                if (device.getFunction() == Function.ENVIRONMENT) {
                    return " (Importante: verificar necessidade de manutenção do equipamento)";
                }
            }
        }
        return "";
    }

    public String getNoteIfHumidityNotWorkedInRange(Device device, List<Reading> readings) {
        double minHumi = device.getMinWorkingHumidity();
        double maxHumi = device.getMaxWorkingHumidity();

        int consecutiveOutOfRangeCount = 0;

        for (Reading reading : readings) {
            if (reading.getValue() < minHumi || reading.getValue() > maxHumi) {
                consecutiveOutOfRangeCount++;
            } else {
                consecutiveOutOfRangeCount = 0;
            }

            if (consecutiveOutOfRangeCount == 7) {
                return " (Importante: valores de umidade fora do limite aceitável foram detectados por, pelo menos, 21 minutos consecutivos. Verifique imediatamente para ajustar as condições)";
            }
        }
        return "";
    }

    private String formatTime(long totalMinutes) {
        long days = totalMinutes / (24 * 60);
        long hours = (totalMinutes % (24 * 60)) / 60;
        long minutes = totalMinutes % 60;

        StringBuilder result = new StringBuilder();
        if (days > 0) {
            result.append(days).append(" dia").append(days > 1 ? "s" : "").append(", ");
        }
        if (hours > 0) {
            result.append(hours).append(" hora").append(hours > 1 ? "s" : "").append(", ");
        }
        result.append(minutes).append(" minuto").append(minutes > 1 ? "s" : "");

        return result.toString();
    }

    private Integer getDay(Instant timestamp) {
        LocalDate date = LocalDate.ofInstant(timestamp, ZoneId.of("America/Sao_Paulo"));
        return date.getDayOfMonth();
    }

    private String getPeriod(Instant timestamp) {
        ZonedDateTime zonedDateTime = timestamp.atZone(ZoneId.of("America/Sao_Paulo"));
        int hour = zonedDateTime.getHour();

        if (hour >= 0 && hour < 4) {
            return "0-4h";
        } else if (hour >= 4 && hour < 8) {
            return "4-8h";
        } else if (hour >= 8 && hour < 12) {
            return "8-12h";
        } else if (hour >= 12 && hour < 16) {
            return "12-16h";
        } else if (hour >= 16 && hour < 20) {
            return "16-20h";
        } else {
            return "20-0h";
        }
    }
}
