package br.com.devlovers.domain.signature.dto;

import br.com.devlovers.domain.signature.validations.Image;
import jakarta.validation.constraints.NotBlank;

public record SignatureUploadDTO(

    @NotBlank
    @Image
    String encodedSignature,

    @NotBlank
    String fileName,

    @NotBlank
    String name
) {}
