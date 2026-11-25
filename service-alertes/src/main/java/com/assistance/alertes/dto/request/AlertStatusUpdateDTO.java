package com.assistance.alertes.dto.request;

import com.assistance.alertes.entity.AlertStatus;
import jakarta.validation.constraints.NotNull;

public class AlertStatusUpdateDTO {

    @NotNull(message = "Le nouveau statut est obligatoire (PRISE_EN_CHARGE ou RESOLUE)")
    private AlertStatus statut;

    private String compteRendu;

    public AlertStatusUpdateDTO() {
    }

    public AlertStatusUpdateDTO(AlertStatus statut, String compteRendu) {
        this.statut = statut;
        this.compteRendu = compteRendu;
    }

    public AlertStatus getStatut() {
        return statut;
    }

    public void setStatut(AlertStatus statut) {
        this.statut = statut;
    }

    public String getCompteRendu() {
        return compteRendu;
    }

    public void setCompteRendu(String compteRendu) {
        this.compteRendu = compteRendu;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private AlertStatus statut;
        private String compteRendu;

        public Builder statut(AlertStatus statut) {
            this.statut = statut;
            return this;
        }

        public Builder compteRendu(String compteRendu) {
            this.compteRendu = compteRendu;
            return this;
        }

        public AlertStatusUpdateDTO build() {
            return new AlertStatusUpdateDTO(statut, compteRendu);
        }
    }
}
