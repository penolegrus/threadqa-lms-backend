package com.lms.repository;

import com.lms.model.Promocode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PromocodeRepository extends JpaRepository<Promocode, Long> {

    Optional<Promocode> findByCodeAndIsActiveTrue(String code);

    @Query("SELECT p FROM Promocode p WHERE p.isActive = true AND p.validFrom <= :now AND (p.validUntil IS NULL OR p.validUntil >= :now)")
    List<Promocode> findAllActivePromocodes(ZonedDateTime now);

    @Query("SELECT p FROM Promocode p WHERE p.isActive = true AND p.code = :code AND p.validFrom <= :now AND (p.validUntil IS NULL OR p.validUntil >= :now) AND (p.maxUses IS NULL OR p.currentUses < p.maxUses)")
    Optional<Promocode> findValidPromocode(String code, ZonedDateTime now);
}