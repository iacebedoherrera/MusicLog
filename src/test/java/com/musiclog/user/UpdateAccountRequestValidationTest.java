package com.musiclog.user;

import static org.assertj.core.api.Assertions.assertThat;

import com.musiclog.user.dto.UpdateAccountRequest;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class UpdateAccountRequestValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void requiresIdentityFieldsWithinTheirLimits() {
        Set<String> fields = validator.validate(new UpdateAccountRequest(
                        " ", "", null, null))
                .stream()
                .map(violation -> violation.getPropertyPath().toString())
                .collect(Collectors.toSet());

        assertThat(fields).contains("displayName", "username");
        assertThat(validator.validate(new UpdateAccountRequest(
                        "d".repeat(101), "u".repeat(51), null, null)))
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("displayName", "username");
    }

    @Test
    void acceptsAnEmptyPasswordPair() {
        assertThat(validator.validate(new UpdateAccountRequest("Ana", "ana", "", ""))).isEmpty();
        assertThat(validator.validate(new UpdateAccountRequest("Ana", "ana", null, null))).isEmpty();
    }

    @Test
    void rejectsAnIncompletePasswordPair() {
        assertThat(validator.validate(new UpdateAccountRequest("Ana", "ana", "p".repeat(8), "")))
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("newPassword", "newPasswordConfirmation");
    }

    @Test
    void rejectsShortAndMismatchedPasswords() {
        assertThat(validator.validate(new UpdateAccountRequest("Ana", "ana", "short", "short")))
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("newPassword", "newPasswordConfirmation");
        assertThat(validator.validate(new UpdateAccountRequest("Ana", "ana", "p".repeat(8), "q".repeat(8))))
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("newPasswordConfirmation");
    }
}
