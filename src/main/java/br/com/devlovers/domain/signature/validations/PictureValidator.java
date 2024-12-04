package br.com.devlovers.domain.signature.validations;

import java.util.Base64;
import java.util.regex.Pattern;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PictureValidator implements ConstraintValidator<Image, String> {
    
    private static final Pattern BASE64_PATTERN = Pattern.compile(
            "^data:image/(png|jpeg|jpg|bmp);base64,[A-Za-z0-9+/]+={0,2}$");
    
    @Override
    public void initialize(Image constraintAnnotation) {
    }
    
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isEmpty()) {
            return false;
        }
        
        if (!BASE64_PATTERN.matcher(value).matches()) {
            return false;
        }
        
        try {
            String base64Image = value.split(",")[1];
            Base64.getDecoder().decode(base64Image);
        } catch (IllegalArgumentException e) {
            return false;
        }
        
        return true;
    }
}
