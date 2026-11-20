package com.telecare.servicenotification.entite;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notification")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Idempotence : un evenement ne produit qu'une seule notification
    @Column(nullable = false, unique = true)
    private String eventId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeNotification type;

    // References distantes : pas de cle etrangere
    private Long alertId;
    private Long interventionId;
    private Long beneficiaryId;

    private String contactNom;
    private String contactTelephone;

    @Column(length = 2000)
    private String message;

    private LocalDateTime dateEnvoi;

    public Notification() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public TypeNotification getType() { return type; }
    public void setType(TypeNotification type) { this.type = type; }

    public Long getAlertId() { return alertId; }
    public void setAlertId(Long alertId) { this.alertId = alertId; }

    public Long getInterventionId() { return interventionId; }
    public void setInterventionId(Long interventionId) { this.interventionId = interventionId; }

    public Long getBeneficiaryId() { return beneficiaryId; }
    public void setBeneficiaryId(Long beneficiaryId) { this.beneficiaryId = beneficiaryId; }

    public String getContactNom() { return contactNom; }
    public void setContactNom(String contactNom) { this.contactNom = contactNom; }

    public String getContactTelephone() { return contactTelephone; }
    public void setContactTelephone(String contactTelephone) { this.contactTelephone = contactTelephone; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public LocalDateTime getDateEnvoi() { return dateEnvoi; }
    public void setDateEnvoi(LocalDateTime dateEnvoi) { this.dateEnvoi = dateEnvoi; }
}