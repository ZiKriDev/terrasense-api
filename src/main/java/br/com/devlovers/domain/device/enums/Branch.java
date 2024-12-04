package br.com.devlovers.domain.device.enums;

public enum Branch {

    NEW_YORK("new_york"),
    SAO_PAULO("sao_paulo"),
    SALVADOR("salvador");

    private String branch;

    Branch(String branch) {
        this.branch = branch;
    }

    public String fromString() {
        return branch;
    }
}
