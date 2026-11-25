package com.assistance.alertes;

import com.assistance.alertes.dto.event.EmergencyResolvedEvent;
import com.assistance.alertes.dto.event.InterventionAssignedEvent;
import com.assistance.alertes.dto.request.AlertCreateDTO;
import com.assistance.alertes.dto.response.AlertResponseDTO;
import com.assistance.alertes.entity.AlertStatus;
import com.assistance.alertes.event.AlertEventConsumer;
import com.assistance.alertes.service.AlertService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@org.springframework.test.context.ActiveProfiles("test")
@SpringBootTest(properties={"spring.cloud.config.enabled=false","spring.config.import="})
@Transactional
class EventConsumerTest {

    @org.springframework.test.context.bean.override.mockito.MockitoBean
    private com.assistance.alertes.client.ResilientBeneficiaryClient beneficiaryClient;
    @org.springframework.test.context.bean.override.mockito.MockitoBean
    private com.assistance.alertes.event.AlertEventPublisher eventPublisher;

    @org.junit.jupiter.api.BeforeEach
    void remoteFixtures() {
        org.mockito.Mockito.when(beneficiaryClient.getBeneficiaryById(org.mockito.ArgumentMatchers.anyLong()))
            .thenAnswer(inv -> new com.assistance.alertes.dto.feign.BeneficiaryDTO(inv.getArgument(0), "M. Robert Martin", 84, "Adresse test", "Pathologie test", "0600000000"));
        org.mockito.Mockito.when(beneficiaryClient.getContactsByBeneficiaryId(org.mockito.ArgumentMatchers.anyLong()))
            .thenAnswer(inv -> java.util.List.of(new com.assistance.alertes.dto.feign.ContactDTO(1L, inv.getArgument(0), "Claire Martin", "Fille", "0600000001")));
    }

    @Autowired
    private AlertService alertService;

    @Autowired
    private AlertEventConsumer alertEventConsumer;

    @Test
    void testAsyncLifecycleWithEvents() {
        // 1. Déclencher une alerte initiale (Statut DECLENCHEE)
        AlertCreateDTO createRequest = AlertCreateDTO.builder()
                .beneficiaryId(200L)
                .type("CHUTE")
                .gravite("ELEVEE")
                .build();
        AlertResponseDTO created = alertService.triggerAlert(createRequest);
        assertEquals(AlertStatus.DECLENCHEE, created.getStatut());

        // 2. Simuler la consommation de l'événement Kafka 'intervention-assigned'
        InterventionAssignedEvent assignedEvent = InterventionAssignedEvent.builder()
                .alertId(created.getId())
                .interventionId(501L)
                .assistantId(12L)
                .timestamp(LocalDateTime.now())
                .build();

        AlertResponseDTO afterAssigned = alertEventConsumer.onInterventionAssigned(assignedEvent);
        assertEquals(AlertStatus.PRISE_EN_CHARGE, afterAssigned.getStatut());

        // Vérifier l'état en base
        AlertResponseDTO inDbAfterAssigned = alertService.getAlertById(created.getId());
        assertEquals(AlertStatus.PRISE_EN_CHARGE, inDbAfterAssigned.getStatut());

        // 3. Simuler la consommation de l'événement Kafka 'emergency-resolved'
        EmergencyResolvedEvent resolvedEvent = EmergencyResolvedEvent.builder()
                .alertId(created.getId())
                .interventionId(501L)
                .compteRendu("Intervention réussie par l'assistant 12. Bénéficiaire en bonne santé.")
                .timestamp(LocalDateTime.now())
                .build();

        AlertResponseDTO afterResolved = alertEventConsumer.onEmergencyResolved(resolvedEvent);
        assertEquals(AlertStatus.RESOLUE, afterResolved.getStatut());
        assertEquals("Intervention réussie par l'assistant 12. Bénéficiaire en bonne santé.", afterResolved.getCompteRendu());

        // Vérifier l'état final en base
        AlertResponseDTO inDbAfterResolved = alertService.getAlertById(created.getId());
        assertEquals(AlertStatus.RESOLUE, inDbAfterResolved.getStatut());
        assertEquals("Intervention réussie par l'assistant 12. Bénéficiaire en bonne santé.", inDbAfterResolved.getCompteRendu());
    }
}
