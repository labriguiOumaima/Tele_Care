package com.assistance.alertes.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "protocols")
public class Protocol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String type; // ex: "CHUTE", "MALAISE", "INTRUSION", "URGENCE_CARDIAQUE"

    @Column(nullable = false, length = 2000)
    private String etapes; // Instructions et étapes d'action à suivre lors du déclenchement

    @Column(nullable = false)
    private Integer niveauPriorite; // 1 (Faible) à 5 (Critique / Immédiat)

    public Protocol() {
    }

    public Protocol(Long id, String type, String etapes, Integer niveauPriorite) {
        this.id = id;
        this.type = type;
        this.etapes = etapes;
        this.niveauPriorite = niveauPriorite;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getEtapes() {
        return etapes;
    }

    public void setEtapes(String etapes) {
        this.etapes = etapes;
    }

    public Integer getNiveauPriorite() {
        return niveauPriorite;
    }

    public void setNiveauPriorite(Integer niveauPriorite) {
        this.niveauPriorite = niveauPriorite;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String type;
        private String etapes;
        private Integer niveauPriorite;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Builder etapes(String etapes) {
            this.etapes = etapes;
            return this;
        }

        public Builder niveauPriorite(Integer niveauPriorite) {
            this.niveauPriorite = niveauPriorite;
            return this;
        }

        public Protocol build() {
            return new Protocol(id, type, etapes, niveauPriorite);
        }
    }
}
