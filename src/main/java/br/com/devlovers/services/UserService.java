package br.com.devlovers.services;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.devlovers.domain.password.dto.UpdatePasswordDTO;
import br.com.devlovers.domain.user.User;
import br.com.devlovers.domain.user.UserById;
import br.com.devlovers.repositories.UserByIdRepository;
import br.com.devlovers.repositories.UserRepository;
import br.com.devlovers.services.exceptions.ResourceNotFoundException;
import br.com.devlovers.services.exceptions.UserAlreadyExistsException;
import reactor.core.publisher.Mono;

@Service
public class UserService {

    @Autowired
    private UserRepository repository;

    @Autowired
    private UserByIdRepository userByIdRepository;

    public Mono<User> findByLogin(String login) {
        return repository.findByKey_Login(login)
                .switchIfEmpty(
                        Mono.error(new ResourceNotFoundException("User not found: User " + login)));
    }

    public Mono<UserById> findById(UUID id) {
        return userByIdRepository.findById(id)
                .switchIfEmpty(
                        Mono.error(new ResourceNotFoundException("User not found: ID " + id)));
    }

    @Transactional
    public Mono<User> insert(User user) {
        return repository.findByKey_Login(user.getKey().getLogin())
                .flatMap(existingUser -> Mono.<User>error(new UserAlreadyExistsException("Email user " + user.getKey().getLogin() + " already exists")))
                .switchIfEmpty(
                    Mono.defer(() -> repository.save(user)
                            .flatMap(savedUser -> userByIdRepository.save(new UserById(savedUser.getKey().getId(), savedUser.getKey().getLogin(), savedUser.getRole()))
                            .thenReturn(savedUser))
                    )
                );
    }

    @Transactional
    public Mono<User> update(String login, UpdatePasswordDTO data) {
        return findByLogin(login)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("User not found: User " + login)))
                .flatMap(user -> {
                    user.update(data);
                    return repository.save(user)
                            .thenReturn(user);
                });
    }
}
