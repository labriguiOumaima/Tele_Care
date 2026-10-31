package com.telecare.beneficiaires.servicebeneficiaires.repository;

import com.telecare.beneficiaires.servicebeneficiaires.entity.Beneficiary;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BeneficiaryRepository
        extends JpaRepository<Beneficiary, Long> {
}