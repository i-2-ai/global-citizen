package com.globalcitizen.countryservice.repository;

import com.globalcitizen.countryservice.model.CitizenPassport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CitizenPassportRepository extends JpaRepository<CitizenPassport, Long> {
    Optional<CitizenPassport> findByCitizenId(String citizenId);
    Optional<CitizenPassport> findByPassportNumber(String passportNumber);
    List<CitizenPassport> findByStatus(CitizenPassport.Status status);
} 