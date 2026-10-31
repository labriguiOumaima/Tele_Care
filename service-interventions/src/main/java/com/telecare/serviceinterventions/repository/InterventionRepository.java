package com.telecare.serviceinterventions.repository;

import com.telecare.serviceinterventions.entite.Intervention;
import com.telecare.serviceinterventions.entite.StatutIntervention;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InterventionRepository extends JpaRepository<Intervention, Long> {

    // Retrouver l'intervention d'une alerte (utile pour l'idempotence, jalon v6)
    Optional<Intervention> findByAlertId(Long alertId);

    boolean existsByAlertId(Long alertId);

    // Filtrer les interventions par statut
    List<Intervention> findByStatut(StatutIntervention statut);
}