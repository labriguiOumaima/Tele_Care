package com.telecare.beneficiaires.servicebeneficiaires.repository;

import com.telecare.beneficiaires.servicebeneficiaires.entity.ContactCreation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContactCreationRepository
        extends JpaRepository<ContactCreation, String> {
}