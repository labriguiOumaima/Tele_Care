package com.assistance.alertes.repository;

import com.assistance.alertes.entity.Protocol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProtocolRepository extends JpaRepository<Protocol, Long> {
    Optional<Protocol> findByTypeIgnoreCase(String type);
    boolean existsByTypeIgnoreCase(String type);
}
