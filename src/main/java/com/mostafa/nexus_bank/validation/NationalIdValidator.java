package com.mostafa.nexus_bank.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class NationalIdValidator implements ConstraintValidator<ValidNationalId, String> {

    private static final Pattern NATIONAL_ID_PATTERN = Pattern.compile("^\\d{14}$");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return NATIONAL_ID_PATTERN.matcher(value).matches();
    }
}
