package br.com.devlovers.infra.security;

import java.security.SecureRandom;
import java.util.Base64;

import org.springframework.stereotype.Service;

@Service
public class KeyGeneratorService {

    public String generate(int keyLength) {
        SecureRandom secureRandom = new SecureRandom();
        byte[] apiKeyBytes = new byte[keyLength];
        secureRandom.nextBytes(apiKeyBytes);

        return Base64.getUrlEncoder().withoutPadding().encodeToString(apiKeyBytes);
    }
}
