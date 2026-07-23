package com.mostafa.nexus_bank.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = IBANValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidIBAN {

    String message() default "IBAN must be a valid Egyptian IBAN (EG followed by 25 digits)";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
