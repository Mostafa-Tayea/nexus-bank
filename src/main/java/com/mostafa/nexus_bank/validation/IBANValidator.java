package com.mostafa.nexus_bank.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class IBANValidator implements ConstraintValidator<ValidIBAN, String> {

    private static final Pattern IBAN_PATTERN = Pattern.compile(
            "^EG\\d{2}[A-Z0-9]{1,34}$"
    );

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return IBAN_PATTERN.matcher(value).matches();
    }
}
