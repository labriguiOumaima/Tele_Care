package com.assistance.alertes.repository;

import com.assistance.alertes.entity.Alert;
import com.assistance.alertes.entity.AlertStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {
    List<Alert> findByStatut(AlertStatus statut);
    List<Alert> findByBeneficiaryId(Long beneficiaryId);
    List<Alert> findByTypeIgnoreCase(String type);
}
