package br.com.devlovers.domain.device.enums;

public enum Function {

    EQUIPMENT("equipment"),
    ENVIRONMENT("environment");

    private String function;

    Function(String function) {
        this.function = function;
    }

    public String fromString() {
        return function;
    }
}
