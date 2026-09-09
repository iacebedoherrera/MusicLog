package com.musiclog.user.dto;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = AccountPasswordPairValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidAccountPasswordPair {

    String message() default "La nueva contraseña y su confirmación deben coincidir.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
