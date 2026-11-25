package com.assistance.alertes.dto.event;

import java.time.LocalDateTime;

public class EmergencyResolvedEvent {
    private Long alertId;
    private Long interventionId;
    private String compteRendu;
    @com.fasterxml.jackson.annotation.JsonFormat(shape = com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING)
    private LocalDateTime timestamp;

    public EmergencyResolvedEvent() {
    }

    public EmergencyResolvedEvent(Long alertId, Long interventionId, String compteRendu, LocalDateTime timestamp) {
        this.alertId = alertId;
        this.interventionId = interventionId;
        this.compteRendu = compteRendu;
        this.timestamp = timestamp;
    }

    public Long getAlertId() {
        return alertId;
    }

    public void setAlertId(Long alertId) {
        this.alertId = alertId;
    }

    public Long getInterventionId() {
        return interventionId;
    }

    public void setInterventionId(Long interventionId) {
        this.interventionId = interventionId;
    }

    public String getCompteRendu() {
        return compteRendu;
    }

    public void setCompteRendu(String compteRendu) {
        this.compteRendu = compteRendu;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long alertId;
        private Long interventionId;
        private String compteRendu;
        @com.fasterxml.jackson.annotation.JsonFormat(shape = com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING)
    private LocalDateTime timestamp;

        public Builder alertId(Long alertId) {
            this.alertId = alertId;
            return this;
        }

        public Builder interventionId(Long interventionId) {
            this.interventionId = interventionId;
            return this;
        }

        public Builder compteRendu(String compteRendu) {
            this.compteRendu = compteRendu;
            return this;
        }

        public Builder timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public EmergencyResolvedEvent build() {
            return new EmergencyResolvedEvent(alertId, interventionId, compteRendu, timestamp);
        }
    }
}
