package com.clinic.validation;

import com.clinic.service.ValidationException;

/**
 * Generic validator interface adhering to Single Responsibility Principle (SRP).
 *
 * @param <T> Type to validate
 */
public interface IValidator<T> {
    void validate(T target) throws ValidationException;
}

