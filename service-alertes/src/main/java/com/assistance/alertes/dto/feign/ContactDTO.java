package com.assistance.alertes.dto.feign;

public class ContactDTO {
    private Long id;
    private Long beneficiaryId;
    private String nom;
    private String lien;
    private String telephone;

    public ContactDTO() {
    }

    public ContactDTO(Long id, Long beneficiaryId, String nom, String lien, String telephone) {
        this.id = id;
        this.beneficiaryId = beneficiaryId;
        this.nom = nom;
        this.lien = lien;
        this.telephone = telephone;
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

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getLien() {
        return lien;
    }

    public void setLien(String lien) {
        this.lien = lien;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private Long beneficiaryId;
        private String nom;
        private String lien;
        private String telephone;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder beneficiaryId(Long beneficiaryId) {
            this.beneficiaryId = beneficiaryId;
            return this;
        }

        public Builder nom(String nom) {
            this.nom = nom;
            return this;
        }

        public Builder lien(String lien) {
            this.lien = lien;
            return this;
        }

        public Builder telephone(String telephone) {
            this.telephone = telephone;
            return this;
        }

        public ContactDTO build() {
            return new ContactDTO(id, beneficiaryId, nom, lien, telephone);
        }
    }
}
