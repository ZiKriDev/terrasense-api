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

@Table("tb_password_reset_token_by_user_id")
@NoArgsConstructor
@EqualsAndHashCode(of = "key")
public class PasswordResetTokenByUserId implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final int EXPIRATION_TIME = 10;

    @PrimaryKeyClass
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    public static class PasswordResetTokenByUserIdKey {
        @PrimaryKeyColumn(name = "userId", type = PrimaryKeyType.PARTITIONED)
        private UUID userId;

        @PrimaryKeyColumn(name = "id", type = PrimaryKeyType.CLUSTERED)
        private UUID id;
    }

    @PrimaryKey
    private PasswordResetTokenByUserIdKey key;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "GMT")
    private Instant expirationTime;

    private String token;

    private String login;

    public PasswordResetTokenByUserId(PasswordResetTokenByUserIdKey key, String token, String login) {
        this.key = key;
        this.token = token;
        this.login = login;
        this.expirationTime = Instant.now().plus(Duration.ofMinutes(EXPIRATION_TIME));
    }

    public PasswordResetTokenByUserId(PasswordResetToken passwordResetToken) {
        this.key = new PasswordResetTokenByUserIdKey(passwordResetToken.getUserId(), passwordResetToken.getId());
        this.token = passwordResetToken.getToken();
        this.login = passwordResetToken.getLogin();
        this.expirationTime = passwordResetToken.getExpirationTime();
    }

    public PasswordResetTokenByUserIdKey getKey() {
        return key;
    }

    public Instant getExpirationTime() {
        return expirationTime;
    }

    public String getToken() {
        return token;
    }

    public String getLogin() {
        return login;
    }

    public void setKey(PasswordResetTokenByUserIdKey key) {
        this.key = key;
    }

    public void setExpirationTime(Instant expirationTime) {
        this.expirationTime = expirationTime;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public boolean isExpired() {
        return Instant.now().isAfter(this.expirationTime);
    }
}
