package br.com.devlovers.services;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import br.com.devlovers.domain.password.dto.UpdatePasswordDTO;
import br.com.devlovers.domain.user.User;
import br.com.devlovers.domain.user.UserById;
import br.com.devlovers.domain.user.enums.Role;
import br.com.devlovers.repositories.UserByIdRepository;
import br.com.devlovers.repositories.UserRepository;
import br.com.devlovers.services.exceptions.ResourceNotFoundException;
import br.com.devlovers.services.exceptions.UserAlreadyExistsException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("dev")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class UserServiceUnitTests {

    @Mock
    private UserRepository repository;

    @Mock
    private UserByIdRepository userByIdRepository;

    @InjectMocks
    private UserService userService;

    private final String LOGIN = "test_user@gmail.com";
    private final UUID USER_ID = UUID.randomUUID();

    private User user;
    private UserById userById;

    @BeforeEach
    void setUp() {
        User.UserKey userKey = new User.UserKey(LOGIN, USER_ID);
        user = new User(userKey, "password", Role.USER);
        userById = new UserById(USER_ID, LOGIN, Role.USER);
    }

    @Test
    @DisplayName("Should find the user by e-mail when everything is OK")
    void findByLoginCase1() {
        when(repository.findByKey_Login(LOGIN)).thenReturn(Mono.just(user));

        StepVerifier.create(userService.findByLogin(LOGIN))
                .expectNext(user)
                .verifyComplete();

        verify(repository, times(1)).findByKey_Login(LOGIN);
    }

    @Test
    @DisplayName("Should not find user by e-mail when it does not exist and then throw exception")
    void findByLoginCase2() {
        when(repository.findByKey_Login(LOGIN)).thenReturn(Mono.empty());

        StepVerifier.create(userService.findByLogin(LOGIN))
                .expectErrorMatches(throwable -> throwable instanceof ResourceNotFoundException)
                .verify();

        verify(repository, times(1)).findByKey_Login(LOGIN);
    }

    @Test
    @DisplayName("Should find the user by ID when everything is OK")
    void findByIdCase1() {
        when(userByIdRepository.findById(USER_ID)).thenReturn(Mono.just(userById));

        StepVerifier.create(userService.findById(USER_ID))
                .expectNext(userById)
                .verifyComplete();

        verify(userByIdRepository, times(1)).findById(USER_ID);
    }

    @Test
    @DisplayName("Should not find user by ID when it does not exist and then throw exception")
    void findByIdCase2() {
        when(userByIdRepository.findById(USER_ID)).thenReturn(Mono.empty());

        StepVerifier.create(userService.findById(USER_ID))
                .expectErrorMatches(throwable -> throwable instanceof ResourceNotFoundException)
                .verify();

        verify(userByIdRepository, times(1)).findById(USER_ID);
    }

    @Test
    @DisplayName("Should create user when everything is OK")
    void insertCase1() {
        when(repository.findByKey_Login(LOGIN)).thenReturn(Mono.empty());
        when(repository.save(any(User.class))).thenReturn(Mono.just(user));
        when(userByIdRepository.save(any(UserById.class))).thenReturn(Mono.empty());

        StepVerifier.create(userService.insert(user))
                .expectNext(user)
                .verifyComplete();

        verify(repository, times(1)).findByKey_Login(LOGIN);
        verify(repository, times(1)).save(user);
        verify(userByIdRepository, times(1)).save(any(UserById.class));
    }

    @Test
    @DisplayName("Should not create user when it does exist and then throw exception")
    void insertCase2() {
        when(repository.findByKey_Login(LOGIN)).thenReturn(Mono.just(user));

        StepVerifier.create(userService.insert(user))
                .expectErrorMatches(throwable -> throwable instanceof UserAlreadyExistsException)
                .verify();

        verify(repository, times(1)).findByKey_Login(LOGIN);
        verify(repository, never()).save(any(User.class));
        verify(userByIdRepository, never()).save(any(UserById.class));
    }

    @Test
    @DisplayName("Should update user data when everything is OK")
    void updateCase1() {
        UpdatePasswordDTO updatePasswordDTO = new UpdatePasswordDTO("newPassword");

        when(repository.findByKey_Login(LOGIN)).thenReturn(Mono.just(user));
        when(repository.save(any(User.class))).thenReturn(Mono.just(user));

        StepVerifier.create(userService.update(LOGIN, updatePasswordDTO))
                .expectNext(user)
                .verifyComplete();

        verify(repository, times(1)).findByKey_Login(LOGIN);
        verify(repository, times(1)).save(user);
    }

    @Test
    @DisplayName("Should not update user when it does not exist and then throw exception")
    void updateCase2() {
        UpdatePasswordDTO updatePasswordDTO = new UpdatePasswordDTO("newPassword");

        when(repository.findByKey_Login(LOGIN)).thenReturn(Mono.empty());

        StepVerifier.create(userService.update(LOGIN, updatePasswordDTO))
                .expectErrorMatches(throwable -> throwable instanceof ResourceNotFoundException)
                .verify();

        verify(repository, times(1)).findByKey_Login(LOGIN);
        verify(repository, never()).save(any(User.class));
    }
}