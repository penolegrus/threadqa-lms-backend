package com.threadqa.lms.repository.user;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;

@Repository
public class UserRepositoryImpl implements UserRepositoryExtensions {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    @Override
    public Long countActiveUsersSince(ZonedDateTime since) {
        Query query = entityManager.createQuery(
                "SELECT COUNT(u) FROM User u WHERE u.lastLoginAt >= :since");
        query.setParameter("since", since);
        return (Long) query.getSingleResult();
    }
    
    @Override
    public String findFullNameById(Long userId) {
        Query query = entityManager.createQuery(
                "SELECT CONCAT(u.firstName, ' ', u.lastName) FROM User u WHERE u.id = :userId");
        query.setParameter("userId", userId);
        return (String) query.getSingleResult();
    }
}
