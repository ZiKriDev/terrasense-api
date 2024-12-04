package br.com.devlovers.domain.register;

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

@Table("tb_user_register_token_by_token")
@NoArgsConstructor
@EqualsAndHashCode(of = "key")
public class UserRegisterTokenByToken implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final int EXPIRATION_TIME = 15;
    
    @PrimaryKeyClass
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    public static class UserRegisterTokenByTokenKey {
        @PrimaryKeyColumn(name = "token", type = PrimaryKeyType.PARTITIONED)
        private String token;

        @PrimaryKeyColumn(name = "id", type = PrimaryKeyType.CLUSTERED)
        private UUID id;
    }

    @PrimaryKey
    private UserRegisterTokenByTokenKey key;

    private String email;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "GMT")
    private Instant expirationTime;

    private Boolean isVerified;

    public UserRegisterTokenByToken(UserRegisterTokenByTokenKey key, String email, Boolean isVerified) {
        this.key = key;
        this.email = email;
        this.isVerified = isVerified;
        this.expirationTime = Instant.now().plus(Duration.ofMinutes(EXPIRATION_TIME));
    }

    public UserRegisterTokenByToken(UserRegisterToken userRegisterToken) {
        this.key = new UserRegisterTokenByTokenKey(userRegisterToken.getToken(), userRegisterToken.getId());
        this.email = userRegisterToken.getEmail();
        this.expirationTime = userRegisterToken.getExpirationTime();
        this.isVerified = userRegisterToken.getIsVerified();
    }

    public UserRegisterTokenByTokenKey getKey() {
        return key;
    }

    public String getEmail() {
        return email;
    }

    public Instant getExpirationTime() {
        return expirationTime;
    }

    public Boolean getIsVerified() {
        return isVerified;
    }

    public void setToken(String email) {
        this.email = email;
    }

    public void setExpirationTime(Instant expirationTime) {
        this.expirationTime = expirationTime;
    }

    public void setIsVerified(Boolean isVerified) {
        this.isVerified = isVerified;
    }

    public boolean isExpired() {
        return Instant.now().isAfter(this.expirationTime);
    }
}
