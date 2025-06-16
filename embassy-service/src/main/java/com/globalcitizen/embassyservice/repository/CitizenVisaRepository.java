package com.globalcitizen.embassyservice.repository;

import com.globalcitizen.embassyservice.model.CitizenVisa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CitizenVisaRepository extends JpaRepository<CitizenVisa, Long> {
    Optional<CitizenVisa> findByVisaId(String visaId);
    List<CitizenVisa> findByCitizenId(String citizenId);
    List<CitizenVisa> findByPassportNumber(String passportNumber);
    List<CitizenVisa> findByStatus(CitizenVisa.Status status);
} 