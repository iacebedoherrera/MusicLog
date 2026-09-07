package com.musiclog.user.dto;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class AccountPasswordPairValidator implements ConstraintValidator<ValidAccountPasswordPair, UpdateAccountRequest> {

    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final int MAX_PASSWORD_LENGTH = 100;

    @Override
    public boolean isValid(UpdateAccountRequest request, ConstraintValidatorContext context) {
        if (request == null) {
            return true;
        }

        String newPassword = request.newPassword();
        String confirmation = request.newPasswordConfirmation();
        boolean passwordEmpty = isEmpty(newPassword);
        boolean confirmationEmpty = isEmpty(confirmation);
        if (passwordEmpty && confirmationEmpty) {
            return true;
        }

        context.disableDefaultConstraintViolation();
        boolean valid = true;
        if (passwordEmpty != confirmationEmpty) {
            String message = "La nueva contraseña y su confirmación deben enviarse juntas.";
            addViolation(context, "newPassword", message);
            addViolation(context, "newPasswordConfirmation", message);
            return false;
        }

        if (newPassword.length() < MIN_PASSWORD_LENGTH || newPassword.length() > MAX_PASSWORD_LENGTH) {
            addViolation(context, "newPassword", "La nueva contraseña debe tener entre 8 y 100 caracteres.");
            valid = false;
        }
        if (confirmation.length() < MIN_PASSWORD_LENGTH || confirmation.length() > MAX_PASSWORD_LENGTH) {
            addViolation(context, "newPasswordConfirmation", "La confirmación debe tener entre 8 y 100 caracteres.");
            valid = false;
        }
        if (!newPassword.equals(confirmation)) {
            addViolation(context, "newPasswordConfirmation", "Las contraseñas no coinciden.");
            valid = false;
        }
        return valid;
    }

    private static boolean isEmpty(String value) {
        return value == null || value.isEmpty();
    }

    private static void addViolation(ConstraintValidatorContext context, String field, String message) {
        context.buildConstraintViolationWithTemplate(message)
                .addPropertyNode(field)
                .addConstraintViolation();
    }
}
