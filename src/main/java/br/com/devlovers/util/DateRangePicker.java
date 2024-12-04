package br.com.devlovers.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class DateRangePicker {


    public static List<LocalDate> generate(Instant start, Instant end) {
        LocalDate startDate = start.atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate endDate = end.atZone(ZoneId.systemDefault()).toLocalDate();

        List<LocalDate> dateRange = new ArrayList<>();
        while (!startDate.isAfter(endDate)) {
            dateRange.add(startDate);
            startDate = startDate.plusDays(1);
        }

        return dateRange;
    }

    public static int getNumberOfDays(LocalDate start, LocalDate end) {
        int count = 0;
    
        while (!start.isAfter(end)) {
            count++;
            start = start.plusDays(1);
        }
    
        return count;
    }

    public static Instant getMoment(LocalDate date) {
        LocalDateTime localDateTime = date.atStartOfDay();
        ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneId.of("America/Sao_Paulo"));
        Instant moment = zonedDateTime.toInstant();

        return moment;
    }
}
