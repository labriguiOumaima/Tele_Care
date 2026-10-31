package com.telecare.beneficiaires.servicebeneficiaires.repository;

import com.telecare.beneficiaires.servicebeneficiaires.entity.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ContactRepository
        extends JpaRepository<Contact, Long> {

    List<Contact> findByBeneficiaryId(Long beneficiaryId);

    boolean existsByBeneficiaryId(Long beneficiaryId);
}
