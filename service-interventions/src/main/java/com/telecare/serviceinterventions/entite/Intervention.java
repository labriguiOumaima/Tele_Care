package com.telecare.serviceinterventions.entite;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "intervention")
public class Intervention {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Référence distante vers l'alerte (service-alertes) : pas de clé étrangère
    @Column(nullable = false, unique = true)
    private Long alertId;

    @Column(nullable = false)
    private String assistantNom;

    private LocalDateTime heureArrivee;

    @Column(length = 2000)
    private String compteRendu;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutIntervention statut = StatutIntervention.AFFECTEE;

    public Intervention() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getAlertId() { return alertId; }
    public void setAlertId(Long alertId) { this.alertId = alertId; }

    public String getAssistantNom() { return assistantNom; }
    public void setAssistantNom(String assistantNom) { this.assistantNom = assistantNom; }

    public LocalDateTime getHeureArrivee() { return heureArrivee; }
    public void setHeureArrivee(LocalDateTime heureArrivee) { this.heureArrivee = heureArrivee; }

    public String getCompteRendu() { return compteRendu; }
    public void setCompteRendu(String compteRendu) { this.compteRendu = compteRendu; }

    public StatutIntervention getStatut() { return statut; }
    public void setStatut(StatutIntervention statut) { this.statut = statut; }
}