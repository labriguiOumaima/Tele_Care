package com.assistance.alertes.dto.event;

import java.time.LocalDateTime;

public class InterventionAssignedEvent {
    private Long alertId;
    private Long interventionId;
    private Long assistantId;
    private LocalDateTime timestamp;

    public InterventionAssignedEvent() {
    }

    public InterventionAssignedEvent(Long alertId, Long interventionId, Long assistantId, LocalDateTime timestamp) {
        this.alertId = alertId;
        this.interventionId = interventionId;
        this.assistantId = assistantId;
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

    public Long getAssistantId() {
        return assistantId;
    }

    public void setAssistantId(Long assistantId) {
        this.assistantId = assistantId;
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
        private Long assistantId;
        private LocalDateTime timestamp;

        public Builder alertId(Long alertId) {
            this.alertId = alertId;
            return this;
        }

        public Builder interventionId(Long interventionId) {
            this.interventionId = interventionId;
            return this;
        }

        public Builder assistantId(Long assistantId) {
            this.assistantId = assistantId;
            return this;
        }

        public Builder timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public InterventionAssignedEvent build() {
            return new InterventionAssignedEvent(alertId, interventionId, assistantId, timestamp);
        }
    }
}
