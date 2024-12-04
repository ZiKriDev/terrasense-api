package br.com.devlovers.domain.user.enums;

public enum Role {
    
    USER("user"),
    ADMIN("admin");

    private String role;

    Role(String role) {
        this.role = role;
    }

    public String toString() {
        return role;
    }
}
