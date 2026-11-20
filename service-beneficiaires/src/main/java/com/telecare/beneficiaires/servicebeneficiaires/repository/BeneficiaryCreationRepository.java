package com.telecare.beneficiaires.servicebeneficiaires.repository;

import com.telecare.beneficiaires.servicebeneficiaires.entity.BeneficiaryCreation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BeneficiaryCreationRepository
        extends JpaRepository<BeneficiaryCreation, String> {
}