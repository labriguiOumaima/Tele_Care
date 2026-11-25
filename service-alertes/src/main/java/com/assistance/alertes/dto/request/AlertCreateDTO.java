package com.assistance.alertes.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class AlertCreateDTO {

    @NotNull(message = "L'identifiant du bénéficiaire (beneficiaryId) est obligatoire")
    private Long beneficiaryId;

    @NotBlank(message = "Le type d'alerte est obligatoire (ex: CHUTE, MALAISE)")
    private String type;

    @NotBlank(message = "La gravité est obligatoire (ex: FAIBLE, MOYENNE, ELEVEE, CRITIQUE)")
    private String gravite;

    public AlertCreateDTO() {
    }

    public AlertCreateDTO(Long beneficiaryId, String type, String gravite) {
        this.beneficiaryId = beneficiaryId;
        this.type = type;
        this.gravite = gravite;
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

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long beneficiaryId;
        private String type;
        private String gravite;

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

        public AlertCreateDTO build() {
            return new AlertCreateDTO(beneficiaryId, type, gravite);
        }
    }
}
