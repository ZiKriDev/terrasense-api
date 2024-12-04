package br.com.devlovers.services.report.model;

import java.time.LocalDate;
import java.util.List;

import br.com.devlovers.domain.device.Device;
import br.com.devlovers.domain.device.enums.Branch;
import br.com.devlovers.domain.reading.Reading;
import br.com.devlovers.domain.reading.enums.ReadingType;

public interface Document {
    float createMetricsForm(int numberOfDays);
    void fillHeader(Branch branch, LocalDate startDate, LocalDate endDate, Device device, ReadingType type);
    void fillMetricsForm(LocalDate startDate, LocalDate endDate, int numberOfDays, List<Reading> readings, ReadingType type);
    float insertChartsAndSignature(ReadingType type, List<Reading> readings, float currentYPosition, String encodedSignature);
    void insertInfoParagraph(Device device, LocalDate startDate, LocalDate endDate, ReadingType readingType, float currentYPosition, List<Reading> readings);
}
