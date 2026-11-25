package com.assistance.alertes.dto.response;

import com.assistance.alertes.dto.feign.BeneficiaryDTO;
import com.assistance.alertes.dto.feign.ContactDTO;
import com.assistance.alertes.entity.AlertStatus;

import java.time.LocalDateTime;
import java.util.List;

public class EnrichedAlertDTO {
    private Long id;
    private Long beneficiaryId;
    private String type;
    private String gravite;
    private LocalDateTime dateDeclenchement;
    private AlertStatus statut;
    private String compteRendu;
    private ProtocolResponseDTO protocol;
    private BeneficiaryDTO beneficiary;
    private List<ContactDTO> emergencyContacts;

    public EnrichedAlertDTO() {
    }

    public EnrichedAlertDTO(Long id, Long beneficiaryId, String type, String gravite,
                            LocalDateTime dateDeclenchement, AlertStatus statut, String compteRendu,
                            ProtocolResponseDTO protocol, BeneficiaryDTO beneficiary,
                            List<ContactDTO> emergencyContacts) {
        this.id = id;
        this.beneficiaryId = beneficiaryId;
        this.type = type;
        this.gravite = gravite;
        this.dateDeclenchement = dateDeclenchement;
        this.statut = statut;
        this.compteRendu = compteRendu;
        this.protocol = protocol;
        this.beneficiary = beneficiary;
        this.emergencyContacts = emergencyContacts;
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

    public ProtocolResponseDTO getProtocol() {
        return protocol;
    }

    public void setProtocol(ProtocolResponseDTO protocol) {
        this.protocol = protocol;
    }

    public BeneficiaryDTO getBeneficiary() {
        return beneficiary;
    }

    public void setBeneficiary(BeneficiaryDTO beneficiary) {
        this.beneficiary = beneficiary;
    }

    public List<ContactDTO> getEmergencyContacts() {
        return emergencyContacts;
    }

    public void setEmergencyContacts(List<ContactDTO> emergencyContacts) {
        this.emergencyContacts = emergencyContacts;
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
        private ProtocolResponseDTO protocol;
        private BeneficiaryDTO beneficiary;
        private List<ContactDTO> emergencyContacts;

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

        public Builder protocol(ProtocolResponseDTO protocol) {
            this.protocol = protocol;
            return this;
        }

        public Builder beneficiary(BeneficiaryDTO beneficiary) {
            this.beneficiary = beneficiary;
            return this;
        }

        public Builder emergencyContacts(List<ContactDTO> emergencyContacts) {
            this.emergencyContacts = emergencyContacts;
            return this;
        }

        public EnrichedAlertDTO build() {
            return new EnrichedAlertDTO(id, beneficiaryId, type, gravite, dateDeclenchement,
                    statut, compteRendu, protocol, beneficiary, emergencyContacts);
        }
    }
}
