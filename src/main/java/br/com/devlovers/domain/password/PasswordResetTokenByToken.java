package br.com.devlovers.domain.password;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table("tb_password_reset_token_by_token")
@NoArgsConstructor
@EqualsAndHashCode(of = "key")
public class PasswordResetTokenByToken implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final int EXPIRATION_TIME = 10;

    @PrimaryKeyClass
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    public static class PasswordResetTokenByTokenKey {
        @PrimaryKeyColumn(name = "token", type = PrimaryKeyType.PARTITIONED)
        private String token;

        @PrimaryKeyColumn(name = "id", type = PrimaryKeyType.CLUSTERED)
        private UUID id;
    }

    @PrimaryKey
    private PasswordResetTokenByTokenKey key;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "GMT")
    private Instant expirationTime;

    // User login
    private String login;

    private UUID userId;

    public PasswordResetTokenByToken(PasswordResetTokenByTokenKey key, String login, UUID userId) {
        this.key = key;
        this.login = login;
        this.userId = userId;
        this.expirationTime = Instant.now().plus(Duration.ofMinutes(EXPIRATION_TIME));
    }

    public PasswordResetTokenByToken(PasswordResetToken passwordResetToken) {
        this.key = new PasswordResetTokenByTokenKey(passwordResetToken.getToken(), passwordResetToken.getId());
        this.login = passwordResetToken.getLogin();
        this.userId = passwordResetToken.getUserId();
        this.expirationTime = passwordResetToken.getExpirationTime();
    }

    public PasswordResetTokenByTokenKey getKey() {
        return key;
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

    public void setKey(PasswordResetTokenByTokenKey key) {
        this.key = key;
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
