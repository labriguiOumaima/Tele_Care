package com.assistance.alertes.dto.response;

import com.assistance.alertes.entity.AlertStatus;
import java.time.LocalDateTime;

public class AlertResponseDTO {
    private Long id;
    private Long beneficiaryId;
    private String type;
    private String gravite;
    private LocalDateTime dateDeclenchement;
    private AlertStatus statut;
    private String compteRendu;

    public AlertResponseDTO() {
    }

    public AlertResponseDTO(Long id, Long beneficiaryId, String type, String gravite,
                            LocalDateTime dateDeclenchement, AlertStatus statut, String compteRendu) {
        this.id = id;
        this.beneficiaryId = beneficiaryId;
        this.type = type;
        this.gravite = gravite;
        this.dateDeclenchement = dateDeclenchement;
        this.statut = statut;
        this.compteRendu = compteRendu;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
        private Long id;
        private Long beneficiaryId;
        private String type;
        private String gravite;
        private LocalDateTime dateDeclenchement;
        private AlertStatus statut;
        private String compteRendu;

        public Builder id(Long id) {
            this.id = id;
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

        public Builder statut(AlertStatus statut) {
            this.statut = statut;
            return this;
        }

        public Builder compteRendu(String compteRendu) {
            this.compteRendu = compteRendu;
            return this;
        }

        public AlertResponseDTO build() {
            return new AlertResponseDTO(id, beneficiaryId, type, gravite, dateDeclenchement, statut, compteRendu);
        }
    }
}
