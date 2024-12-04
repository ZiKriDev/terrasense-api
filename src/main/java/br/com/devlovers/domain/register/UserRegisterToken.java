package br.com.devlovers.domain.register;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Table("tb_user_register_token")
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class UserRegisterToken implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final int EXPIRATION_TIME = 15;

    @PrimaryKey
    private UUID id;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "GMT")
    private Instant expirationTime;

    private String email;
    private String token;

    private Boolean isVerified;

    public UserRegisterToken(UUID id, String email, String token, Boolean isVerified) {
        this.id = id;
        this.email = email;
        this.token = token;
        this.isVerified = isVerified;
        this.expirationTime = Instant.now().plus(Duration.ofMinutes(EXPIRATION_TIME));
    }

    public UUID getId() {
        return id;
    }

    public String getToken() {
        return token;
    }

    public Instant getExpirationTime() {
        return expirationTime;
    }

    public String getEmail() {
        return email;
    }

    public Boolean getIsVerified() {
        return isVerified;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setExpirationTime(Instant expirationTime) {
        this.expirationTime = expirationTime;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setIsVerified(Boolean isVerified) {
        this.isVerified = isVerified;
    }

    public boolean isExpired() {
        return Instant.now().isAfter(this.expirationTime);
    }
}
