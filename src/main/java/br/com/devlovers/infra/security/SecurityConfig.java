package br.com.devlovers.infra.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Autowired
    private SecurityFilter securityFilter;

    @Autowired
    private AuthorizationService authorizationService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity httpSecurity) {
        return httpSecurity
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .pathMatchers(HttpMethod.POST, "/api/auth/verify-email", "/api/auth/sign-in", "/api/auth/sign-up", "/api/devices/**", "/api/password/request-reset", "/api/password/reset").permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/validate-token", "/api/auth/isVerifiedEmail", "/api/auth/isVerifiedToken").permitAll()

                        .pathMatchers(HttpMethod.POST, "/api/auth/sign-out").authenticated()

                        .pathMatchers(HttpMethod.POST, "/api/devices", "/api/users/**", "/api/devices/change-sensor/**", "/api/reports/period").hasRole("USER")
                        .pathMatchers(HttpMethod.GET, "/api/devices/**", "/api/users/**").hasRole("USER")
                        .pathMatchers(HttpMethod.PUT, "/api/devices/**").hasRole("USER")
                        .pathMatchers(HttpMethod.DELETE, "/api/devices/**").hasRole("USER")
                        .anyExchange().authenticated())
                .addFilterBefore(securityFilter, SecurityWebFiltersOrder.AUTHORIZATION)
                .build();
    }

    @Bean
    public ReactiveAuthenticationManager reactiveAuthenticationManager() {
        UserDetailsRepositoryReactiveAuthenticationManager manager = new UserDetailsRepositoryReactiveAuthenticationManager(
                authorizationService);
        manager.setPasswordEncoder(passwordEncoder);
        return manager;
    }
}
