package com.telecare.servicenotification.service;

import com.telecare.servicenotification.entite.Notification;
import com.telecare.servicenotification.entite.TypeNotification;
import com.telecare.servicenotification.event.EmergencyResolvedEvent;
import com.telecare.servicenotification.event.InterventionAssignedEvent;
import com.telecare.servicenotification.exception.RessourceIntrouvableException;
import com.telecare.servicenotification.feignClient.ContactDto;
import com.telecare.servicenotification.feignClient.ResilientRequests;
import com.telecare.servicenotification.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final ResilientRequests resilientRequests;
    private final org.springframework.mail.javamail.JavaMailSender mailSender;
    @org.springframework.beans.factory.annotation.Value("${app.mail.from:telecare@localhost.test}")
    private String from;

    public NotificationService(NotificationRepository notificationRepository,
                               ResilientRequests resilientRequests, org.springframework.mail.javamail.JavaMailSender mailSender) {
        this.notificationRepository = notificationRepository;
        this.resilientRequests = resilientRequests;
        this.mailSender = mailSender;
    }

    // --- Consultation ---
    public List<Notification> lister(Long alertId) {
        if (alertId == null) {
            return notificationRepository.findAll();
        }
        return notificationRepository.findByAlertId(alertId);
    }

    public Notification getById(Long id) {
        return notificationRepository.findById(id)
                .orElseThrow(() -> new RessourceIntrouvableException("Notification introuvable : " + id));
    }

    // --- Evenement intervention-assigned : l'alerte est prise en charge ---
    public void notifierPriseEnCharge(InterventionAssignedEvent event) {
        String message = "L'alerte " + event.alertId() + " est prise en charge : l'assistant "
                + event.assistantId() + " est en route.";
        traiter(event.eventId(), TypeNotification.PRISE_EN_CHARGE,
                event.alertId(), event.interventionId(), event.beneficiaryId(), message);
    }

    // --- Evenement emergency-resolved : l'urgence est cloturee ---
    public void notifierResolution(EmergencyResolvedEvent event) {
        String message = "L'alerte " + event.alertId() + " est resolue. Compte-rendu : "
                + event.compteRendu();
        traiter(event.eventId(), TypeNotification.RESOLUTION,
                event.alertId(), event.interventionId(), event.beneficiaryId(), message);
    }

    private synchronized void traiter(String eventId, TypeNotification type, Long alertId,
                         Long interventionId, Long beneficiaryId, String message) {
        if (eventId == null || beneficiaryId == null) {
            log.error("Evenement {} ignore : eventId ou beneficiaryId manquant (alertId {})", type, alertId);
            return;
        }

        // 1. Idempotence : evenement deja traite, on n'envoie rien de plus
        if (notificationRepository.existsByEventId(eventId)) {
            log.info("Evenement {} deja traite, message ignore", eventId);
            return;
        }

        // 2. Trouver le contact de confiance chez service-beneficiaires
        List<ContactDto> contacts = resilientRequests.getContacts(beneficiaryId);
        if (contacts == null || contacts.isEmpty()) {
            log.warn("Aucun contact pour le beneficiaire {} : notification impossible (alertId {})",
                    beneficiaryId, alertId);
            return;
        }
        // Un contact n'a pas d'indicateur "de confiance" : on prend le premier
        ContactDto contact = contacts.stream().filter(c -> c.email() != null && !c.email().isBlank()).findFirst()
            .orElseThrow(() -> new IllegalStateException("Aucun contact avec email pour le beneficiaire " + beneficiaryId));
        org.springframework.mail.SimpleMailMessage mail = new org.springframework.mail.SimpleMailMessage();
        mail.setFrom(from);
        mail.setTo(contact.email());
        mail.setSubject("Tele-Care : " + type + " / alerte " + alertId);
        mail.setText(message);
        mailSender.send(mail);

        // 3. Envoi SMTP puis enregistrement apres confirmation du serveur SMTP
        Notification notification = new Notification();
        notification.setEventId(eventId);
        notification.setType(type);
        notification.setAlertId(alertId);
        notification.setInterventionId(interventionId);
        notification.setBeneficiaryId(beneficiaryId);
        notification.setContactNom(contact.nom());
        notification.setContactTelephone(contact.telephone());
        notification.setContactEmail(contact.email());
        notification.setMessage(message);
        notification.setDateEnvoi(LocalDateTime.now());
        notificationRepository.save(notification);

        log.info("NOTIFICATION {} envoyee a {} ({}) : {}",
                type, contact.nom(), contact.telephone(), message);
    }
}