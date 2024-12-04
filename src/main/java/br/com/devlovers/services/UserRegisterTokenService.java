package br.com.devlovers.services;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileCopyUtils;
import org.springframework.core.io.Resource;

import br.com.devlovers.domain.register.UserRegisterToken;
import br.com.devlovers.domain.register.UserRegisterTokenByEmail;
import br.com.devlovers.domain.register.UserRegisterTokenByToken;
import br.com.devlovers.infra.security.KeyGeneratorService;
import br.com.devlovers.repositories.UserRegisterTokenByEmailRepository;
import br.com.devlovers.repositories.UserRegisterTokenByTokenRepository;
import br.com.devlovers.repositories.UserRegisterTokenRepository;
import br.com.devlovers.services.exceptions.EmailSendingException;
import br.com.devlovers.services.exceptions.ResourceNotFoundException;
import br.com.devlovers.services.exceptions.UserAlreadyExistsException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import reactor.core.publisher.Mono;

import org.thymeleaf.context.Context;

@Service
public class UserRegisterTokenService {

    @Autowired
    private UserRegisterTokenRepository byIdRepository;

    @Autowired
    private UserRegisterTokenByEmailRepository byEmailRepository;

    @Autowired
    private UserRegisterTokenByTokenRepository byTokenRepository;

    @Autowired
    private KeyGeneratorService keyGeneratorService;

    @Autowired
    private JavaMailSender mailSender;

    @Transactional
    public Mono<Void> createRegisterTokenForUser(String email) {
        String token = keyGeneratorService.generate(8);

        return byEmailRepository.findByKeyEmail(email)
                .flatMap(existingToken -> {
                    if (existingToken.getIsVerified()) {
                        return Mono.error(new UserAlreadyExistsException(
                                "User with " + email + " e-mail already existing and verified"));
                    }

                    existingToken.setToken(token);
                    existingToken.setExpirationTime(Instant.now().plus(Duration.ofMinutes(15)));
                    existingToken.setIsVerified(false);

                    UserRegisterToken updatedToken = new UserRegisterToken(existingToken.getKey().getId(), email, token,
                            false);

                    UserRegisterTokenByToken tokenByToken = new UserRegisterTokenByToken(updatedToken);
                    UserRegisterTokenByEmail tokenByEmail = new UserRegisterTokenByEmail(updatedToken);

                    return Mono.zip(
                            byIdRepository.save(updatedToken),
                            byEmailRepository.save(tokenByEmail),
                            byTokenRepository.save(tokenByToken)).then();
                })
                .switchIfEmpty(Mono.defer(() -> {
                    UUID newTokenId = UUID.randomUUID();
                    UserRegisterToken newToken = new UserRegisterToken(newTokenId, email, token, false);
                    UserRegisterTokenByEmail newTokenByEmail = new UserRegisterTokenByEmail(newToken);
                    UserRegisterTokenByToken newTokenByToken = new UserRegisterTokenByToken(newToken);

                    return Mono.zip(
                            byIdRepository.save(newToken),
                            byEmailRepository.save(newTokenByEmail),
                            byTokenRepository.save(newTokenByToken)).then();
                }))
                .then(Mono.fromRunnable(() -> sendRegisterTokenEmail(email, token)));
    }

    public Mono<UserRegisterToken> findUserRegisterTokenByToken(String token) {
        return byTokenRepository.findByKeyToken(token)
                .flatMap(tokenEntity -> byIdRepository.findById(tokenEntity.getKey().getId()));
    }

    public Mono<UserRegisterToken> findUserRegisterTokenByEmail(String email) {
        return byEmailRepository.findByKeyEmail(email)
                .flatMap(emailEntity -> byIdRepository.findById(emailEntity.getKey().getId()));
    }

    @Transactional
    public Mono<UserRegisterToken> update(UUID id, Boolean isVerified) {
        return byIdRepository.findById(id)
                .flatMap(entity -> {
                    entity.setIsVerified(isVerified);

                    UserRegisterTokenByEmail updatedTokenByEmail = new UserRegisterTokenByEmail(entity);
                    UserRegisterTokenByToken updatedTokenByToken = new UserRegisterTokenByToken(entity);

                    return Mono.zip(
                            byIdRepository.save(entity),
                            byEmailRepository.save(updatedTokenByEmail),
                            byTokenRepository.save(updatedTokenByToken)).map(tuple -> tuple.getT1());
                })
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Registration token not found: ID " + id)));
    }

    private void sendRegisterTokenEmail(String email, String token) {
        Context context = new Context();
        context.setVariable("token", token);

        MimeMessage mimeMessage = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setTo(email);
            helper.setSubject("(TerraSense) Email Verification");

            ClassPathResource image = new ClassPathResource("/static/images/logo-social.png");

            helper.setText(getHtmlContent(token), true);
            helper.addInline("terraSenseLogo", image);

            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new EmailSendingException("Failed to send verification email to " + email, e);
        }
    }

    private String getHtmlContent(String token) {
        try {
            Resource resource = new ClassPathResource("templates/user-register-email.html");
            byte[] bdata = FileCopyUtils.copyToByteArray(resource.getInputStream());
            String content = new String(bdata, StandardCharsets.UTF_8);

            return content.replace("${token}", token);
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }
}
