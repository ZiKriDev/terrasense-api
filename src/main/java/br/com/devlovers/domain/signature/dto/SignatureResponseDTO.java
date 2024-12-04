package br.com.devlovers.domain.signature.dto;

public record SignatureResponseDTO(

    String name,
    String fileName,
    String encodedSignature
) {

}
