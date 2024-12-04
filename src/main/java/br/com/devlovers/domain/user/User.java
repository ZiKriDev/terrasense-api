package br.com.devlovers.domain.user;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.annotation.Transient;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import br.com.devlovers.domain.password.dto.UpdatePasswordDTO;
import br.com.devlovers.domain.user.enums.Role;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Table("tb_users")
@EqualsAndHashCode(of = "key")
public class User implements UserDetails {

    @PrimaryKeyClass
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    public static class UserKey {

        @PrimaryKeyColumn(type = PrimaryKeyType.PARTITIONED)
        private String login;

        @PrimaryKeyColumn(type = PrimaryKeyType.CLUSTERED)
        private UUID id;
    }

    @PrimaryKey
    private UserKey key;

    private String password;
    private Role role;

    @Transient
    private Collection<GrantedAuthority> authorities;

    public User(UserKey key, String password, Role role) {
        this.key = key;
        this.password = password;
        this.role = role;
    }

    public UserKey getKey() {
        return key;
    }

    public UUID getId() {
        return key.getId();
    }

    public Role getRole() {
        return role;
    }

    public void setKey(UserKey key) {
        this.key = key;
    }

    public void setId(UUID id) {
        key.setId(id);
    }

    public void setLogin(String login) {
        key.setLogin(login);
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    @Override
    public Collection<GrantedAuthority> getAuthorities() {
        if (this.role == Role.ADMIN)
            return List.of(new SimpleGrantedAuthority("ROLE_ADMIN"),
                    new SimpleGrantedAuthority("ROLE_USER"));
        else
            return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getUsername() {
        return key.getLogin();
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public void update(UpdatePasswordDTO data) {
        if (data.password() != null) {
            this.password = data.password();
        }
    }
}
