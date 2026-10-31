package com.telecare.serviceinterventions.service;

import com.telecare.serviceinterventions.entite.Intervention;
import com.telecare.serviceinterventions.entite.StatutIntervention;
import com.telecare.serviceinterventions.exception.InterventionExistanteException;
import com.telecare.serviceinterventions.exception.RessourceIntrouvableException;
import com.telecare.serviceinterventions.exception.TransitionInterditeException;
import com.telecare.serviceinterventions.repository.InterventionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class InterventionService {

    private final InterventionRepository interventionRepository;

    public InterventionService(InterventionRepository interventionRepository) {
        this.interventionRepository = interventionRepository;
    }

    public List<Intervention> getAllInterventions(StatutIntervention statut) {
        if (statut == null) {
            return interventionRepository.findAll();
        }
        return interventionRepository.findByStatut(statut);
    }

    public Intervention getInterventionById(Long id) {
        return interventionRepository.findById(id)
                .orElseThrow(() -> new RessourceIntrouvableException("Intervention introuvable : " + id));
    }

    @Transactional
    public Intervention creerIntervention(Intervention intervention) {
        if (interventionRepository.existsByAlertId(intervention.getAlertId())) {
            throw new InterventionExistanteException(intervention.getAlertId());
        }
        intervention.setId(null);
        intervention.setStatut(StatutIntervention.AFFECTEE);
        return interventionRepository.save(intervention);
    }

    @Transactional
    public Intervention enregistrerArrivee(Long id, LocalDateTime heureArrivee) {
        Intervention intervention = getInterventionById(id);
        if (intervention.getStatut() != StatutIntervention.AFFECTEE) {
            throw new TransitionInterditeException(
                    "L'arrivee ne peut etre enregistree que pour une intervention AFFECTEE (statut actuel : "
                            + intervention.getStatut() + ")");
        }
        intervention.setHeureArrivee(heureArrivee != null ? heureArrivee : LocalDateTime.now());
        intervention.setStatut(StatutIntervention.SUR_PLACE);
        return interventionRepository.save(intervention);
    }

    @Transactional
    public Intervention redigerCompteRendu(Long id, String compteRendu) {
        Intervention intervention = getInterventionById(id);
        if (intervention.getStatut() != StatutIntervention.SUR_PLACE) {
            throw new TransitionInterditeException(
                    "Le compte-rendu ne peut etre redige que pour une intervention SUR_PLACE (statut actuel : "
                            + intervention.getStatut() + ")");
        }
        intervention.setCompteRendu(compteRendu);
        return interventionRepository.save(intervention);
    }

    @Transactional
    public Intervention cloturer(Long id) {
        Intervention intervention = getInterventionById(id);
        if (intervention.getStatut() != StatutIntervention.SUR_PLACE) {
            throw new TransitionInterditeException(
                    "La cloture n'est possible que pour une intervention SUR_PLACE (statut actuel : "
                            + intervention.getStatut() + ")");
        }
        if (intervention.getCompteRendu() == null || intervention.getCompteRendu().isBlank()) {
            throw new TransitionInterditeException("Le compte-rendu doit etre redige avant la cloture");
        }
        intervention.setStatut(StatutIntervention.CLOTUREE);
        return interventionRepository.save(intervention);
    }

    @Transactional
    public void supprimerIntervention(Long id) {
        Intervention intervention = getInterventionById(id);
        if (intervention.getStatut() == StatutIntervention.CLOTUREE) {
            throw new TransitionInterditeException("Une intervention CLOTUREE ne peut pas etre supprimee");
        }
        interventionRepository.delete(intervention);
    }
}