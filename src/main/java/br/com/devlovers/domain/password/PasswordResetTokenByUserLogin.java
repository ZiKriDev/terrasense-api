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

@Table("tb_password_reset_token_by_user_login")
@NoArgsConstructor
@EqualsAndHashCode(of = "key")
public class PasswordResetTokenByUserLogin implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final int EXPIRATION_TIME = 10;

    @PrimaryKeyClass
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    public static class PasswordResetTokenByUserLoginKey {
        @PrimaryKeyColumn(name = "login", type = PrimaryKeyType.PARTITIONED)
        private String login;

        @PrimaryKeyColumn(name = "id", type = PrimaryKeyType.CLUSTERED)
        private UUID id;
    }

    @PrimaryKey
    private PasswordResetTokenByUserLoginKey key;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "GMT")
    private Instant expirationTime;

    private String token;

    public PasswordResetTokenByUserLogin(PasswordResetTokenByUserLoginKey key, String token) {
        this.key = key;
        this.token = token;
        this.expirationTime = Instant.now().plus(Duration.ofMinutes(EXPIRATION_TIME));
    }

    public PasswordResetTokenByUserLoginKey getKey() {
        return key;
    }

    public Instant getExpirationTime() {
        return expirationTime;
    }

    public String getToken() {
        return token;
    }

    public void setKey(PasswordResetTokenByUserLoginKey key) {
        this.key = key;
    }

    public void setExpirationTime(Instant expirationTime) {
        this.expirationTime = expirationTime;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public boolean isExpired() {
        return Instant.now().isAfter(this.expirationTime);
    }
}
