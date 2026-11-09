package com.clinic.repository;

import java.util.List;
import java.util.Optional;

/**
 * Generic repository interface adhering to Interface Segregation Principle (ISP)
 * and Dependency Inversion Principle (DIP).
 *
 * @param <T>  Entity type
 * @param <ID> Primary identifier type
 */
public interface IRepository<T, ID> {
    T save(T entity);
    Optional<T> findById(ID id);
    List<T> findAll();
    boolean existsById(ID id);
    void deleteById(ID id);
    long count();
}

