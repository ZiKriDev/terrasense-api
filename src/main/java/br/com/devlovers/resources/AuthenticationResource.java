package br.com.devlovers.resources;

import java.net.URI;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import br.com.devlovers.domain.register.dto.UserIsVerifiedEmailOrTokenResponseDTO;
import br.com.devlovers.domain.register.dto.UserRegisterDTO;
import br.com.devlovers.domain.register.dto.UserRequestVerificationEmailDTO;
import br.com.devlovers.domain.user.User;
import br.com.devlovers.domain.user.User.UserKey;
import br.com.devlovers.domain.user.dto.LoginRequestDTO;
import br.com.devlovers.domain.user.dto.LoginResponseDTO;
import br.com.devlovers.domain.user.dto.UserResponseAuthDTO;
import br.com.devlovers.domain.user.dto.UserResponseDTO;
import br.com.devlovers.domain.user.enums.Role;
import br.com.devlovers.infra.security.TokenService;
import br.com.devlovers.resources.exceptions.StandardError;
import br.com.devlovers.services.UserRegisterTokenService;
import br.com.devlovers.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(value = "/api/auth")
public class AuthenticationResource {

    @Autowired
    private UserService userService;

    @Autowired
    private ReactiveAuthenticationManager authenticationManager;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private UserRegisterTokenService userRegisterTokenService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping(value = "/isVerifiedEmail")
    @Operation(summary = "Busca o estado do e-mail, se está verificado ou não")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "E-mail já verificado ou não", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserIsVerifiedEmailOrTokenResponseDTO.class, name = "IsEmailVerifiedResponse"))),
        @ApiResponse(responseCode = "404", description = "E-mail não encontrado e não verificado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserIsVerifiedEmailOrTokenResponseDTO.class, name = "IsEmailVerifiedResponse")))
    })
    public Mono<ResponseEntity<UserIsVerifiedEmailOrTokenResponseDTO>> checkIfEmailIsVerified(
            @RequestParam(required = true) String email) {

        return this.userRegisterTokenService.findUserRegisterTokenByEmail(email)
                .map(regToken -> {
                    UserIsVerifiedEmailOrTokenResponseDTO responseDTO = new UserIsVerifiedEmailOrTokenResponseDTO(
                            regToken);
                    return ResponseEntity.ok().body(responseDTO);
                })
                .switchIfEmpty(Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new UserIsVerifiedEmailOrTokenResponseDTO(false))));
    }

    @GetMapping(value = "/isVerifiedToken")
    @Operation(summary = "Busca o estado do token de registro, se já foi verificado ou não")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Token já verificado ou não", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserIsVerifiedEmailOrTokenResponseDTO.class, name = "IsTokenVerifiedResponse"))),
        @ApiResponse(responseCode = "404", description = "Token não encontrado e não verificado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserIsVerifiedEmailOrTokenResponseDTO.class, name = "IsTokenVerifiedResponse")))
    })
    public Mono<ResponseEntity<UserIsVerifiedEmailOrTokenResponseDTO>> checkIfRegisterTokenIsVerified(
            @RequestParam(required = true) String token) {

        return this.userRegisterTokenService.findUserRegisterTokenByToken(token)
                .map(regToken -> {
                    UserIsVerifiedEmailOrTokenResponseDTO responseDTO = new UserIsVerifiedEmailOrTokenResponseDTO(
                            regToken);
                    return ResponseEntity.ok().body(responseDTO);
                })
                .switchIfEmpty(Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new UserIsVerifiedEmailOrTokenResponseDTO(false))));
    }

    @PostMapping(value = "/verify-email")
    @Operation(summary = "Envia um e-mail de verificação")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "E-mail enviado com sucesso", content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "400", description = "Erro de validação", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardError.class))),
        @ApiResponse(responseCode = "409", description = "Usuário já existente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardError.class))),
        @ApiResponse(responseCode = "500", description = "Erro ao enviar e-mail de verificação", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardError.class)))
    })
    public Mono<ResponseEntity<Void>> requestVerificationEmail(
            @RequestBody @Valid UserRequestVerificationEmailDTO request) {
        return userRegisterTokenService.createRegisterTokenForUser(request.email())
                .then(Mono.just(ResponseEntity.ok().build()));
    }

    @PostMapping(value = "/sign-in")
    @Operation(summary = "Faz login no sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login realizado com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = LoginResponseDTO.class, name = "UserLoginResponse"))),
        @ApiResponse(responseCode = "400", description = "Erro de validação", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardError.class))),
        @ApiResponse(responseCode = "404", description = "Credenciais inválidas", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardError.class))),
        @ApiResponse(responseCode = "500", description = "Erro ao gerar token", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardError.class)))
    })
    public Mono<ResponseEntity<LoginResponseDTO>> signIn(@RequestBody @Valid LoginRequestDTO data) {
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                data.login(), data.password());
        Mono<Authentication> auth = this.authenticationManager.authenticate(usernamePasswordAuthenticationToken);

        Mono<User> userMono = auth.map(authentication -> {
            Object principal = authentication.getPrincipal();

            return (User) principal;
        });

        return userMono.map(user -> {
            String token = tokenService.generateToken(user);
            UserResponseAuthDTO userResponseAuthDTO = new UserResponseAuthDTO(user);

            return ResponseEntity.ok(new LoginResponseDTO(token, userResponseAuthDTO));
        });
    }

    @PostMapping(value = "/sign-up")
    @Operation(summary = "Realiza cadastro no sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Cadastro realizado com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponseDTO.class, name = "UserResponse"))),
        @ApiResponse(responseCode = "400", description = "Erro de validação", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardError.class))),
        @ApiResponse(responseCode = "409", description = "Usuário já existente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardError.class)))
    })
    public Mono<ResponseEntity<UserResponseDTO>> signUp(@RequestBody @Valid UserRegisterDTO data) {
        return userRegisterTokenService.findUserRegisterTokenByToken(data.token())
                .flatMap(registerToken -> {
                    String email = registerToken.getEmail();
                    UUID tokenId = registerToken.getId();
                    String encryptedPassword = passwordEncoder.encode(data.password());

                    UserKey key = new UserKey(email, UUID.randomUUID());
                    User newUser = new User(key, encryptedPassword, Role.USER);

                    return userService.insert(newUser)
                            .flatMap(savedUser -> userRegisterTokenService.update(tokenId, true)
                                    .then(Mono.just(savedUser)))
                            .map(savedUser -> {
                                URI uri = UriComponentsBuilder.fromPath("/api/users/{id}")
                                        .buildAndExpand(savedUser.getId())
                                        .toUri();

                                UserResponseDTO userResponseDTO = new UserResponseDTO(savedUser);
                                return ResponseEntity.created(uri).body(userResponseDTO);
                            });
                });
    }

    @PostMapping(value = "/sign-out")
    @Operation(summary = "Realiza logout com segurança")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Logout bem-sucedido", content = @Content(mediaType = "application/json"))
    })
    public Mono<ResponseEntity<Void>> signOut(@RequestHeader("Authorization") String authorizationHeader) {
        String token = authorizationHeader.replace("Bearer ", "");

        return Mono.fromRunnable(() -> {
            tokenService.addTokenBlacklist(token);
            SecurityContextHolder.clearContext();
        }).then(Mono.just(ResponseEntity.noContent().build()));
    }
}