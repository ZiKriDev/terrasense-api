package br.com.devlovers.services;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileCopyUtils;
import org.thymeleaf.context.Context;

import br.com.devlovers.domain.password.PasswordResetToken;
import br.com.devlovers.domain.password.PasswordResetTokenByToken;
import br.com.devlovers.domain.password.PasswordResetTokenByToken.PasswordResetTokenByTokenKey;
import br.com.devlovers.domain.password.PasswordResetTokenByUserId;
import br.com.devlovers.domain.password.PasswordResetTokenByUserLogin;
import br.com.devlovers.domain.password.PasswordResetTokenByUserLogin.PasswordResetTokenByUserLoginKey;
import br.com.devlovers.domain.user.User;
import br.com.devlovers.infra.security.KeyGeneratorService;
import br.com.devlovers.repositories.PasswordResetTokenByTokenRepository;
import br.com.devlovers.repositories.PasswordResetTokenByUserIdRepository;
import br.com.devlovers.repositories.PasswordResetTokenByUserLoginRepository;
import br.com.devlovers.repositories.PasswordResetTokenRepository;
import br.com.devlovers.services.exceptions.EmailSendingException;
import br.com.devlovers.services.exceptions.ResourceNotFoundException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import reactor.core.publisher.Mono;

@Service
public class PasswordResetTokenService {

    @Autowired
    private PasswordResetTokenByTokenRepository byTokenRepository;

    @Autowired
    private PasswordResetTokenRepository byIdRepository;

    @Autowired
    private PasswordResetTokenByUserIdRepository byUserIdRepository;

    @Autowired
    private PasswordResetTokenByUserLoginRepository byUserLoginRepository;

    @Autowired
    private KeyGeneratorService keyGeneratorService;

    @Autowired
    private JavaMailSender mailSender;

    @Transactional
    public Mono<Void> createPasswordTokenForUser(User user) {
        String token = keyGeneratorService.generate(8);

        return byUserIdRepository.findByKeyUserId(user.getId())
                .flatMap(existingToken -> {
                    return Mono.zip(
                            byIdRepository.deleteById(user.getId()),
                            byTokenRepository.deleteById(new PasswordResetTokenByTokenKey(token, existingToken.getKey().getId())),
                            byUserIdRepository.delete(existingToken),
                            byUserLoginRepository.deleteById(new PasswordResetTokenByUserLoginKey(user.getUsername(), existingToken.getKey().getId()))).then();
                })
                // After removal, create and persist the new token
                .then(Mono.defer(() -> {
                    UUID newTokenId = UUID.randomUUID();
                    Instant expirationTime = Instant.now().plus(Duration.ofMinutes(10));

                    PasswordResetToken newToken = new PasswordResetToken(newTokenId, token, user.getUsername(),
                            user.getId());
                    newToken.setExpirationTime(expirationTime);
                    
                    PasswordResetTokenByToken newTokenByToken = new PasswordResetTokenByToken(newToken);
                    PasswordResetTokenByUserId newTokenByUserId = new PasswordResetTokenByUserId(newToken);
                    PasswordResetTokenByUserLoginKey newKeyForTokenByUserLogin = new PasswordResetTokenByUserLoginKey(
                            user.getUsername(), newTokenId);
                    PasswordResetTokenByUserLogin newTokenByUserLogin = new PasswordResetTokenByUserLogin(
                            newKeyForTokenByUserLogin, token);

                    return Mono.zip(
                            byIdRepository.save(newToken),
                            byTokenRepository.save(newTokenByToken),
                            byUserIdRepository.save(newTokenByUserId),
                            byUserLoginRepository.save(newTokenByUserLogin)).then();
                }))
                .then(Mono.fromRunnable(() -> sendResetTokenEmail(user.getUsername(), token)));
    }

    public Mono<PasswordResetToken> findPasswordResetTokenByToken(String token) {
        return byTokenRepository.findByKeyToken(token)
                .flatMap(tokenEntity -> byIdRepository.findById(tokenEntity.getKey().getId()))
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Invalid token: Token " + token)));
    }

    private void sendResetTokenEmail(String email, String token) {
        Context context = new Context();
        context.setVariable("token", token);

        MimeMessage mimeMessage = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setTo(email);
            helper.setSubject("(TerraSense) Password Reset");

            ClassPathResource image = new ClassPathResource("/static/images/logo-social.png");

            helper.setText(getHtmlContent(token), true);
            helper.addInline("terraSenseLogo", image);

            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new EmailSendingException("Failed to send recovery email to " + email, e);
        }
    }

    private String getHtmlContent(String token) {
        try {
            Resource resource = new ClassPathResource("templates/password-reset-email.html");
            byte[] bdata = FileCopyUtils.copyToByteArray(resource.getInputStream());
            String content = new String(bdata, StandardCharsets.UTF_8);

            return content.replace("${token}", token);
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }
}
