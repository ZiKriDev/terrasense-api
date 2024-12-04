package br.com.devlovers.domain.device.enums;

public enum DeviceType {

    THR316("thr316"),
    THR316D("thr316d"),
    TH16RF("th16rf"),
    ESP32("esp32"),
    ESP8266("esp8266"),
    ESP12("esp12"),
    TH("th");

    private String type;

    DeviceType(String type) {
        this.type = type;
    }

    public String fromString() {
        return type;
    }
}
