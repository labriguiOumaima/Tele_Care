package com.telecare.servicenotification.dto;

import com.telecare.servicenotification.entite.Notification;
import com.telecare.servicenotification.entite.TypeNotification;

import java.time.LocalDateTime;

public record NotificationDto(
        Long id,
        String eventId,
        TypeNotification type,
        Long alertId,
        Long interventionId,
        Long beneficiaryId,
        String contactNom,
        String contactTelephone,
        String contactEmail,
        String message,
        LocalDateTime dateEnvoi
) {
    public static NotificationDto from(Notification n) {
        return new NotificationDto(
                n.getId(),
                n.getEventId(),
                n.getType(),
                n.getAlertId(),
                n.getInterventionId(),
                n.getBeneficiaryId(),
                n.getContactNom(),
                n.getContactTelephone(),
                n.getContactEmail(),
                n.getMessage(),
                n.getDateEnvoi());
    }
}