package br.com.devlovers.domain.signature.validations;

import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Constraint(validatedBy = PictureValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface Image {
    String message() default "Invalid image format. Allowed formats: PNG, JPEG, JPG, BMP";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
