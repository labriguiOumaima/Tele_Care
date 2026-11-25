package com.assistance.alertes.dto.event;

import java.time.LocalDateTime;

public class AlertTriggeredEvent {
    private Long alertId;
    private Long beneficiaryId;
    private String type;
    private String gravite;
    @com.fasterxml.jackson.annotation.JsonFormat(shape = com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING)
    private LocalDateTime dateDeclenchement;
    @com.fasterxml.jackson.annotation.JsonFormat(shape = com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING)
    private LocalDateTime timestamp;

    public AlertTriggeredEvent() {
    }

    public AlertTriggeredEvent(Long alertId, Long beneficiaryId, String type, String gravite,
                               LocalDateTime dateDeclenchement, LocalDateTime timestamp) {
        this.alertId = alertId;
        this.beneficiaryId = beneficiaryId;
        this.type = type;
        this.gravite = gravite;
        this.dateDeclenchement = dateDeclenchement;
        this.timestamp = timestamp;
    }

    public Long getAlertId() {
        return alertId;
    }

    public void setAlertId(Long alertId) {
        this.alertId = alertId;
    }

    public Long getBeneficiaryId() {
        return beneficiaryId;
    }

    public void setBeneficiaryId(Long beneficiaryId) {
        this.beneficiaryId = beneficiaryId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getGravite() {
        return gravite;
    }

    public void setGravite(String gravite) {
        this.gravite = gravite;
    }

    public LocalDateTime getDateDeclenchement() {
        return dateDeclenchement;
    }

    public void setDateDeclenchement(LocalDateTime dateDeclenchement) {
        this.dateDeclenchement = dateDeclenchement;
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
        private Long beneficiaryId;
        private String type;
        private String gravite;
        @com.fasterxml.jackson.annotation.JsonFormat(shape = com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING)
    private LocalDateTime dateDeclenchement;
        @com.fasterxml.jackson.annotation.JsonFormat(shape = com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING)
    private LocalDateTime timestamp;

        public Builder alertId(Long alertId) {
            this.alertId = alertId;
            return this;
        }

        public Builder beneficiaryId(Long beneficiaryId) {
            this.beneficiaryId = beneficiaryId;
            return this;
        }

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Builder gravite(String gravite) {
            this.gravite = gravite;
            return this;
        }

        public Builder dateDeclenchement(LocalDateTime dateDeclenchement) {
            this.dateDeclenchement = dateDeclenchement;
            return this;
        }

        public Builder timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public AlertTriggeredEvent build() {
            return new AlertTriggeredEvent(alertId, beneficiaryId, type, gravite, dateDeclenchement, timestamp);
        }
    }
}
