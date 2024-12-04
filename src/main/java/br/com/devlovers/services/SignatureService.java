package br.com.devlovers.services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.devlovers.domain.signature.Signature;
import br.com.devlovers.domain.signature.SignatureByName;
import br.com.devlovers.domain.signature.dto.SignatureResponseDTO;
import br.com.devlovers.repositories.SignatureByNameRepository;
import br.com.devlovers.repositories.SignatureRepository;
import br.com.devlovers.repositories.UserByIdRepository;
import br.com.devlovers.services.exceptions.FileException;
import br.com.devlovers.services.exceptions.ResourceNotFoundException;
import br.com.devlovers.services.exceptions.SignatureAlreadyExistsException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class SignatureService {

    @Value("${img.storage.path}")
    private String storageDirectory;

    @Autowired
    private SignatureRepository repository;

    @Autowired
    private SignatureByNameRepository byNameRepository;

    @Autowired
    private UserByIdRepository userByIdRepository;

    @Transactional
    public Mono<Signature> saveSignature(UUID userId, String base64Data, String originalFilename, String name) {
        return byNameRepository.findByKeyNameAndKeyUserId(name, userId)
                .hasElement()
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new SignatureAlreadyExistsException(
                                "Você não pode ter duas assinaturas com o mesmo nome"));
                    }

                    return userByIdRepository.findById(userId)
                            .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                                    "Usuário de ID " + userId + " não encontrado")))
                            .then(Mono.defer(() -> {
                                String signatureExtension = extractFileExtension(base64Data);
                                String base64ImageData = base64Data.substring(base64Data.indexOf(",") + 1);
                                byte[] decodedBytes = Base64.getDecoder().decode(base64ImageData);

                                String fileName = UUID.randomUUID().toString() + "_" + originalFilename + "."
                                        + signatureExtension;
                                Path filePath = Paths.get(storageDirectory + fileName);

                                try {
                                    Files.write(filePath, decodedBytes);
                                } catch (IOException e) {
                                    return Mono.error(new FileException("Falha ao salvar assinatura"));
                                }

                                Signature signature = new Signature(
                                        new Signature.SignatureKey(userId, UUID.randomUUID()),
                                        filePath.toString(),
                                        fileName,
                                        signatureExtension,
                                        name);

                                SignatureByName signatureByName = new SignatureByName(signature);

                                return repository.save(signature)
                                        .then(byNameRepository.save(signatureByName))
                                        .thenReturn(signature);
                            }));
                });
    }

    public Flux<SignatureResponseDTO> getSignaturesByUserId(UUID userId) {
        return userByIdRepository.findById(userId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Usuário de ID " + userId + " não encontrado")))
                .thenMany(repository.findByKeyUserId(userId)
                        .flatMap(signature -> {
                            Path filePath = Paths.get(signature.getFilePath());
                            try {
                                byte[] fileBytes = Files.readAllBytes(filePath);
                                String file = Base64.getEncoder().encodeToString(fileBytes);
                                String encodedSignature = "data:image/" + signature.getFileExtension() + ";base64,"
                                        + file;

                                SignatureResponseDTO dto = new SignatureResponseDTO(
                                        signature.getName(),
                                        signature.getFileName(),
                                        encodedSignature);

                                return Mono.just(dto);
                            } catch (IOException e) {
                                return Mono.error(new FileException("Falha ao carregar assinatura"));
                            }
                        }));
    }

    public Mono<SignatureResponseDTO> getSignatureByNameAndUserId(String name, UUID userId) {
        return byNameRepository.findByKeyNameAndKeyUserId(name, userId)
                .flatMap(signatureByName -> {
                    Path filePath = Paths.get(signatureByName.getFilePath());
                    try {
                        byte[] fileBytes = Files.readAllBytes(filePath);
                        String encodedSignature = "data:image/" + signatureByName.getFileExtension() + ";base64," +
                                Base64.getEncoder().encodeToString(fileBytes);

                        return Mono.just(new SignatureResponseDTO(
                                signatureByName.getKey().getName(),
                                signatureByName.getFileName(),
                                encodedSignature));
                    } catch (IOException e) {
                        return Mono.error(new FileException("Falha ao carregar assinatura"));
                    }
                })
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                        "Assinatura de nome " + name + " não encontrada")));
    }

    private String extractFileExtension(String base64Data) {
        if (base64Data.startsWith("data:image/")) {
            int startIndex = base64Data.indexOf("/") + 1;
            int endIndex = base64Data.indexOf(";");
            return base64Data.substring(startIndex, endIndex);
        }
        return "";
    }
}
