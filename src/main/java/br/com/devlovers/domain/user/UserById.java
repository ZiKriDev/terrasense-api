package br.com.devlovers.domain.user;

import java.util.UUID;

import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import br.com.devlovers.domain.user.enums.Role;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table("tb_users_by_id")
@EqualsAndHashCode(of = "id")
public class UserById {

    @PrimaryKey
    private UUID id;

    private String login;
    private Role role;

}
