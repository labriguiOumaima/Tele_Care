package com.assistance.alertes.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ProtocolRequestDTO {

    @NotBlank(message = "Le type d'alerte du protocole est obligatoire (ex: CHUTE, MALAISE)")
    private String type;

    @NotBlank(message = "Les étapes du protocole sont obligatoires")
    private String etapes;

    @NotNull(message = "Le niveau de priorité est obligatoire (1 à 5)")
    @Min(value = 1, message = "Le niveau de priorité minimum est 1")
    @Max(value = 5, message = "Le niveau de priorité maximum est 5")
    private Integer niveauPriorite;

    public ProtocolRequestDTO() {
    }

    public ProtocolRequestDTO(String type, String etapes, Integer niveauPriorite) {
        this.type = type;
        this.etapes = etapes;
        this.niveauPriorite = niveauPriorite;
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
        private String type;
        private String etapes;
        private Integer niveauPriorite;

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

        public ProtocolRequestDTO build() {
            return new ProtocolRequestDTO(type, etapes, niveauPriorite);
        }
    }
}
