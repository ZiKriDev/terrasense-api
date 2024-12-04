package br.com.devlovers.domain.reading.enums;

public enum ReadingType {
    
    TEMPERATURE("temperature"),
    HUMIDITY("humidity");

    private String readingType;

    ReadingType(String readingType) {
        this.readingType = readingType;
    }

    public String fromString() {
        return readingType;
    }
}
