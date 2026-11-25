package com.assistance.alertes.dto.response;

public class ProtocolResponseDTO {
    private Long id;
    private String type;
    private String etapes;
    private Integer niveauPriorite;

    public ProtocolResponseDTO() {
    }

    public ProtocolResponseDTO(Long id, String type, String etapes, Integer niveauPriorite) {
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

        public ProtocolResponseDTO build() {
            return new ProtocolResponseDTO(id, type, etapes, niveauPriorite);
        }
    }
}
