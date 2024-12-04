package br.com.devlovers.infra.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import br.com.devlovers.repositories.UserRepository;
import reactor.core.publisher.Mono;

@Service
public class AuthorizationService implements ReactiveUserDetailsService {

    @Autowired
    private UserRepository repository;

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return repository.findByKey_Login(username)
                         .map(user -> (UserDetails) user)
                         .switchIfEmpty(Mono.error(new UsernameNotFoundException("O usuário ou senha fornecidos estão incorretos")));
    }
}

