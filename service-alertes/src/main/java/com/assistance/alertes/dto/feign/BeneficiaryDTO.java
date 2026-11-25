package com.assistance.alertes.dto.feign;

public class BeneficiaryDTO {
    private Long id;
    private String nom;
    private Integer age;
    private String adresse;
    private String pathologie;
    private String telephone;

    public BeneficiaryDTO() {
    }

    public BeneficiaryDTO(Long id, String nom, Integer age, String adresse, String pathologie, String telephone) {
        this.id = id;
        this.nom = nom;
        this.age = age;
        this.adresse = adresse;
        this.pathologie = pathologie;
        this.telephone = telephone;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getPathologie() {
        return pathologie;
    }

    public void setPathologie(String pathologie) {
        this.pathologie = pathologie;
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
        private String nom;
        private Integer age;
        private String adresse;
        private String pathologie;
        private String telephone;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder nom(String nom) {
            this.nom = nom;
            return this;
        }

        public Builder age(Integer age) {
            this.age = age;
            return this;
        }

        public Builder adresse(String adresse) {
            this.adresse = adresse;
            return this;
        }

        public Builder pathologie(String pathologie) {
            this.pathologie = pathologie;
            return this;
        }

        public Builder telephone(String telephone) {
            this.telephone = telephone;
            return this;
        }

        public BeneficiaryDTO build() {
            return new BeneficiaryDTO(id, nom, age, adresse, pathologie, telephone);
        }
    }
}
