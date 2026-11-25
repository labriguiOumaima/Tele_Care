package com.assistance.alertes;

import com.assistance.alertes.dto.request.AlertCreateDTO;
import com.assistance.alertes.dto.request.AlertStatusUpdateDTO;
import com.assistance.alertes.dto.response.AlertResponseDTO;
import com.assistance.alertes.dto.response.EnrichedAlertDTO;
import com.assistance.alertes.entity.AlertStatus;
import com.assistance.alertes.exception.BadRequestException;
import com.assistance.alertes.service.AlertService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class AlertServiceTest {

    @Autowired
    private AlertService alertService;

    @Test
    void testTriggerAlert() {
        AlertCreateDTO request = AlertCreateDTO.builder()
                .beneficiaryId(99L)
                .type("CHUTE")
                .gravite("CRITIQUE")
                .build();

        AlertResponseDTO alert = alertService.triggerAlert(request);

        assertNotNull(alert.getId());
        assertEquals(99L, alert.getBeneficiaryId());
        assertEquals("CHUTE", alert.getType());
        assertEquals("CRITIQUE", alert.getGravite());
        assertEquals(AlertStatus.DECLENCHEE, alert.getStatut());
        assertNotNull(alert.getDateDeclenchement());
    }

    @Test
    void testTriggerAlertWithIncompatibleProtocolThrowsException() {
        AlertCreateDTO request = AlertCreateDTO.builder()
                .beneficiaryId(99L)
                .type("TYPE_INCOMPATIBLE_SANS_PROTOCOLE")
                .gravite("CRITIQUE")
                .build();

        assertThrows(BadRequestException.class, () -> {
            alertService.triggerAlert(request);
        });
    }

    @Test
    void testGetEnrichedAlertById() {
        // Alerte ID 1 pré-remplie par DataInitializer (type CHUTE)
        EnrichedAlertDTO enriched = alertService.getEnrichedAlertById(1L);

        assertNotNull(enriched);
        assertEquals(1L, enriched.getId());
        assertEquals("CHUTE", enriched.getType());

        // Vérification du protocole associé
        assertNotNull(enriched.getProtocol());
        assertEquals("CHUTE", enriched.getProtocol().getType());
        assertEquals(4, enriched.getProtocol().getNiveauPriorite());
        assertNotNull(enriched.getProtocol().getEtapes());

        // Vérification du bénéficiaire simulé / Feign
        assertNotNull(enriched.getBeneficiary());
        assertEquals("M. Robert Martin", enriched.getBeneficiary().getNom());
        assertEquals(84, enriched.getBeneficiary().getAge());
        assertNotNull(enriched.getBeneficiary().getPathologie());

        // Vérification des contacts d'urgence
        assertNotNull(enriched.getEmergencyContacts());
        assertFalse(enriched.getEmergencyContacts().isEmpty());
        assertEquals("Claire Martin", enriched.getEmergencyContacts().get(0).getNom());
    }

    @Test
    void testFilterAlertsByStatus() {
        List<AlertResponseDTO> declencheAlerts = alertService.getAlerts(AlertStatus.DECLENCHEE);
        assertNotNull(declencheAlerts);
        for (AlertResponseDTO a : declencheAlerts) {
            assertEquals(AlertStatus.DECLENCHEE, a.getStatut());
        }

        List<AlertResponseDTO> chargeAlerts = alertService.getAlerts(AlertStatus.PRISE_EN_CHARGE);
        assertNotNull(chargeAlerts);
        for (AlertResponseDTO a : chargeAlerts) {
            assertEquals(AlertStatus.PRISE_EN_CHARGE, a.getStatut());
        }
    }

    @Test
    void testLifecycleTransitions() {
        // 1. Déclencher une alerte
        AlertCreateDTO createRequest = AlertCreateDTO.builder()
                .beneficiaryId(105L)
                .type("MALAISE")
                .gravite("ELEVEE")
                .build();
        AlertResponseDTO alert = alertService.triggerAlert(createRequest);
        assertEquals(AlertStatus.DECLENCHEE, alert.getStatut());

        // 2. Passage à PRISE_EN_CHARGE
        AlertStatusUpdateDTO priseEnChargeRequest = AlertStatusUpdateDTO.builder()
                .statut(AlertStatus.PRISE_EN_CHARGE)
                .build();
        AlertResponseDTO inCharge = alertService.updateAlertStatus(alert.getId(), priseEnChargeRequest);
        assertEquals(AlertStatus.PRISE_EN_CHARGE, inCharge.getStatut());

        // 3. Clôture avec compte-rendu -> RESOLUE
        AlertStatusUpdateDTO resolutionRequest = AlertStatusUpdateDTO.builder()
                .statut(AlertStatus.RESOLUE)
                .compteRendu("Intervention réussie sans incident.")
                .build();
        AlertResponseDTO resolved = alertService.updateAlertStatus(alert.getId(), resolutionRequest);
        assertEquals(AlertStatus.RESOLUE, resolved.getStatut());
        assertEquals("Intervention réussie sans incident.", resolved.getCompteRendu());

        // 4. Tentative de modifier une alerte déjà résolue -> doit échouer
        assertThrows(BadRequestException.class, () -> {
            alertService.updateAlertStatus(alert.getId(), priseEnChargeRequest);
        });
    }
}
