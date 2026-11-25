package com.assistance.alertes.service;

import com.assistance.alertes.client.BeneficiaryClient;
import com.assistance.alertes.client.BeneficiaryClientFallback;
import com.assistance.alertes.dto.feign.BeneficiaryDTO;
import com.assistance.alertes.dto.feign.ContactDTO;
import com.assistance.alertes.dto.request.AlertCreateDTO;
import com.assistance.alertes.dto.request.AlertStatusUpdateDTO;
import com.assistance.alertes.dto.response.AlertResponseDTO;
import com.assistance.alertes.dto.response.EnrichedAlertDTO;
import com.assistance.alertes.dto.response.ProtocolResponseDTO;
import com.assistance.alertes.entity.Alert;
import com.assistance.alertes.entity.AlertStatus;
import com.assistance.alertes.entity.Protocol;
import com.assistance.alertes.event.AlertEventPublisher;
import com.assistance.alertes.exception.BadRequestException;
import com.assistance.alertes.exception.ResourceNotFoundException;
import com.assistance.alertes.repository.AlertRepository;
import com.assistance.alertes.repository.ProtocolRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class AlertService {

    private static final Logger log = LoggerFactory.getLogger(AlertService.class);

    private final AlertRepository alertRepository;
    private final ProtocolRepository protocolRepository;
    private final BeneficiaryClient beneficiaryClient;
    private final BeneficiaryClientFallback beneficiaryClientFallback;
    private final AlertEventPublisher alertEventPublisher;

    public AlertService(AlertRepository alertRepository,
                        ProtocolRepository protocolRepository,
                        BeneficiaryClient beneficiaryClient,
                        BeneficiaryClientFallback beneficiaryClientFallback,
                        AlertEventPublisher alertEventPublisher) {
        this.alertRepository = alertRepository;
        this.protocolRepository = protocolRepository;
        this.beneficiaryClient = beneficiaryClient;
        this.beneficiaryClientFallback = beneficiaryClientFallback;
        this.alertEventPublisher = alertEventPublisher;
    }

    /**
     * Déclenche une nouvelle alerte avec :
     * 1. Vérification de la compatibilité du protocole avec le type d'alerte
     * 2. Validation préalable de l'existence du bénéficiaire via OpenFeign
     * 3. Statut initial obligatoire : DECLENCHEE
     * 4. Horodatage automatique
     * 5. Publication de l'événement Kafka alert-triggered (Phase V5)
     */
    public AlertResponseDTO triggerAlert(AlertCreateDTO request) {
        String normalizedType = request.getType().trim().toUpperCase();

        // Vérification de compatibilité de protocole
        if (!protocolRepository.existsByTypeIgnoreCase(normalizedType)) {
            throw new BadRequestException("Type d'alerte non reconnu. Aucun protocole d'urgence n'est configuré pour: " + normalizedType);
        }

        // Validation préalable du bénéficiaire distant (Feign)
        BeneficiaryDTO beneficiary = getBeneficiarySafe(request.getBeneficiaryId());
        if (beneficiary == null) {
            throw new ResourceNotFoundException("Bénéficiaire introuvable avec l'id: " + request.getBeneficiaryId());
        }

        Alert alert = Alert.builder()
                .beneficiaryId(request.getBeneficiaryId())
                .type(normalizedType)
                .gravite(request.getGravite().trim().toUpperCase())
                .dateDeclenchement(LocalDateTime.now())
                .statut(AlertStatus.DECLENCHEE)
                .compteRendu(null)
                .build();

        Alert saved = alertRepository.save(alert);

        // Publication de l'événement Kafka alert-triggered
        alertEventPublisher.publishAlertTriggered(saved);

        return mapToResponse(saved);
    }

    /**
     * Consultation d'une alerte enrichie (Phase V4) :
     * Agrège Alerte + Protocole de réponse + Dossier bénéficiaire + Contacts d'urgence
     */
    @Transactional(readOnly = true)
    public EnrichedAlertDTO getEnrichedAlertById(Long id) {
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alerte non trouvée avec l'id: " + id));

        // 1. Récupération du protocole associé
        ProtocolResponseDTO protocolDTO = protocolRepository.findByTypeIgnoreCase(alert.getType())
                .map(p -> ProtocolResponseDTO.builder()
                        .id(p.getId())
                        .type(p.getType())
                        .etapes(p.getEtapes())
                        .niveauPriorite(p.getNiveauPriorite())
                        .build())
                .orElse(null);

        // 2. Récupération distante du bénéficiaire (via OpenFeign)
        BeneficiaryDTO beneficiary = getBeneficiarySafe(alert.getBeneficiaryId());

        // 3. Récupération distante des contacts d'urgence (via OpenFeign)
        List<ContactDTO> contacts = getContactsSafe(alert.getBeneficiaryId());

        return EnrichedAlertDTO.builder()
                .id(alert.getId())
                .beneficiaryId(alert.getBeneficiaryId())
                .type(alert.getType())
                .gravite(alert.getGravite())
                .dateDeclenchement(alert.getDateDeclenchement())
                .statut(alert.getStatut())
                .compteRendu(alert.getCompteRendu())
                .protocol(protocolDTO)
                .beneficiary(beneficiary)
                .emergencyContacts(contacts)
                .build();
    }

    /**
     * Récupère les alertes, avec filtrage optionnel par statut.
     */
    @Transactional(readOnly = true)
    public List<AlertResponseDTO> getAlerts(AlertStatus statut) {
        List<Alert> alerts;
        if (statut != null) {
            alerts = alertRepository.findByStatut(statut);
        } else {
            alerts = alertRepository.findAll();
        }
        return alerts.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Récupère une alerte par son identifiant unique.
     */
    @Transactional(readOnly = true)
    public AlertResponseDTO getAlertById(Long id) {
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alerte non trouvée avec l'id: " + id));
        return mapToResponse(alert);
    }

    /**
     * Fait évoluer le cycle de vie de l'alerte :
     * DECLENCHEE -> PRISE_EN_CHARGE -> RESOLUE
     */
    public AlertResponseDTO updateAlertStatus(Long id, AlertStatusUpdateDTO request) {
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alerte non trouvée avec l'id: " + id));

        AlertStatus currentStatus = alert.getStatut();
        AlertStatus newStatus = request.getStatut();

        if (currentStatus == AlertStatus.RESOLUE) {
            throw new BadRequestException("Une alerte résolue ne peut plus changer de statut.");
        }

        if (currentStatus == AlertStatus.DECLENCHEE && newStatus != AlertStatus.PRISE_EN_CHARGE && newStatus != AlertStatus.RESOLUE) {
            throw new BadRequestException("Transition de statut invalide. Une alerte DECLENCHEE peut passer à PRISE_EN_CHARGE ou RESOLUE.");
        }

        if (currentStatus == AlertStatus.PRISE_EN_CHARGE && newStatus != AlertStatus.RESOLUE) {
            throw new BadRequestException("Transition de statut invalide. Une alerte PRISE_EN_CHARGE ne peut passer qu'à RESOLUE.");
        }

        alert.setStatut(newStatus);
        if (request.getCompteRendu() != null && !request.getCompteRendu().trim().isEmpty()) {
            alert.setCompteRendu(request.getCompteRendu().trim());
        }

        Alert updated = alertRepository.save(alert);
        return mapToResponse(updated);
    }

    /**
     * Supprime une alerte.
     */
    public void deleteAlert(Long id) {
        if (!alertRepository.existsById(id)) {
            throw new ResourceNotFoundException("Alerte non trouvée avec l'id: " + id);
        }
        alertRepository.deleteById(id);
    }

    public AlertResponseDTO mapToResponse(Alert alert) {
        return AlertResponseDTO.builder()
                .id(alert.getId())
                .beneficiaryId(alert.getBeneficiaryId())
                .type(alert.getType())
                .gravite(alert.getGravite())
                .dateDeclenchement(alert.getDateDeclenchement())
                .statut(alert.getStatut())
                .compteRendu(alert.getCompteRendu())
                .build();
    }

    /**
     * Appel Feign résilient avec Circuit Breaker et Retry (Jalon V6)
     * Bascule automatique vers le fallback en cas d'indisponibilité de service-beneficiaires.
     */
    @CircuitBreaker(name = "beneficiaires-service", fallbackMethod = "fallbackBeneficiary")
    @Retry(name = "beneficiaires-service")
    public BeneficiaryDTO getBeneficiarySafe(Long id) {
        BeneficiaryDTO b = beneficiaryClient.getBeneficiaryById(id);
        if (b != null) {
            return b;
        }
        return beneficiaryClientFallback.getBeneficiaryById(id);
    }

    public BeneficiaryDTO fallbackBeneficiary(Long id, Throwable t) {
        log.warn("[RESILIENCE4J] Circuit Breaker / Retry activé sur getBeneficiarySafe(id={}) : {}", id, t.getMessage());
        return beneficiaryClientFallback.getBeneficiaryById(id);
    }

    @CircuitBreaker(name = "beneficiaires-service", fallbackMethod = "fallbackContacts")
    @Retry(name = "beneficiaires-service")
    public List<ContactDTO> getContactsSafe(Long id) {
        List<ContactDTO> contacts = beneficiaryClient.getContactsByBeneficiaryId(id);
        if (contacts != null && !contacts.isEmpty()) {
            return contacts;
        }
        return beneficiaryClientFallback.getContactsByBeneficiaryId(id);
    }

    public List<ContactDTO> fallbackContacts(Long id, Throwable t) {
        log.warn("[RESILIENCE4J] Circuit Breaker / Retry activé sur getContactsSafe(id={}) : {}", id, t.getMessage());
        return beneficiaryClientFallback.getContactsByBeneficiaryId(id);
    }
}
