package br.com.devlovers.domain.device.enums;

public enum Sensor {

    DS18B20("ds18b20"),
    AM2301("am2301"),
    SI7021("si7021"),
    DHT11("dht11");

    private String sensor;

    Sensor(String sensor) {
        this.sensor = sensor;
    }

    public String fromString() {
        return sensor;
    }
}
