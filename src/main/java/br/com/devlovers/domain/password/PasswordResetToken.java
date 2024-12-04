package br.com.devlovers.domain.password;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Table("tb_password_reset_token")
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class PasswordResetToken implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final int EXPIRATION_TIME = 10;

    @PrimaryKey
    private UUID id;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "GMT")
    private Instant expirationTime;

    private String token;

    // User login
    private String login;

    private UUID userId;

    public PasswordResetToken(UUID id, String token, String login, UUID userId) {
        this.id = id;
        this.token = token;
        this.login = login;
        this.userId = userId;
        this.expirationTime = Instant.now().plus(Duration.ofMinutes(EXPIRATION_TIME));
    }

    public UUID getId() {
        return id;
    }

    public String getToken() {
        return token;
    }

    public String getLogin() {
        return login;
    }

    public Instant getExpirationTime() {
        return expirationTime;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public void setExpirationTime(Instant expirationTime) {
        this.expirationTime = expirationTime;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public boolean isExpired() {
        return Instant.now().isAfter(this.expirationTime);
    }
}
