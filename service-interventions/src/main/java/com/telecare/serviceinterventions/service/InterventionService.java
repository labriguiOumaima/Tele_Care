package com.telecare.serviceinterventions.service;

import com.telecare.serviceinterventions.dto.InterventionDetailDto;
import com.telecare.serviceinterventions.entite.Intervention;
import com.telecare.serviceinterventions.entite.StatutIntervention;
import com.telecare.serviceinterventions.exception.InterventionExistanteException;
import com.telecare.serviceinterventions.exception.RessourceIntrouvableException;
import com.telecare.serviceinterventions.exception.TransitionInterditeException;
import com.telecare.serviceinterventions.feignClient.AlerteDto;
import com.telecare.serviceinterventions.feignClient.AlerteRequest;
import com.telecare.serviceinterventions.feignClient.ProtocolDto;
import com.telecare.serviceinterventions.feignClient.ProtocolRequest;
import com.telecare.serviceinterventions.repository.InterventionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class InterventionService {

    private final InterventionRepository interventionRepository;
    private final AlerteRequest alerteRequest;
    private final ProtocolRequest protocolRequest;

    public InterventionService(InterventionRepository interventionRepository,
                               AlerteRequest alerteRequest,
                               ProtocolRequest protocolRequest) {
        this.interventionRepository = interventionRepository;
        this.alerteRequest = alerteRequest;
        this.protocolRequest = protocolRequest;
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

    @Transactional(readOnly = true)
    public InterventionDetailDto getInterventionDetail(Long id) {
        Intervention intervention = getInterventionById(id);
        AlerteDto alerte = alerteRequest.getAlerteById(intervention.getAlertId());
        ProtocolDto protocole = protocolRequest.getProtocolByType(alerte.type());
        return new InterventionDetailDto(
                intervention.getId(),
                intervention.getAlertId(),
                intervention.getAssistantNom(),
                intervention.getHeureArrivee(),
                intervention.getCompteRendu(),
                intervention.getStatut(),
                alerte.type(),
                alerte.gravite(),
                alerte.statut(),
                protocole.etapes(),
                protocole.niveauPriorite());
    }

    @Transactional
    public Intervention creerIntervention(Intervention intervention) {
        // 1. Idempotence : l'alertId sert de cle. Verification AVANT tout appel externe.
        if (interventionRepository.existsByAlertId(intervention.getAlertId())) {
            throw new InterventionExistanteException(intervention.getAlertId());
        }

        // 2. L'alerte doit exister chez service-alertes (404 si introuvable, 503 si injoignable)
        AlerteDto alerte = alerteRequest.getAlerteById(intervention.getAlertId());

        // 3. L'alerte doit etre DECLENCHEE (sinon elle est deja prise en charge ou resolue)
        if (!"DECLENCHEE".equals(alerte.statut())) {
            throw new TransitionInterditeException(
                    "L'alerte " + alerte.id() + " n'est pas DECLENCHEE (statut actuel : " + alerte.statut() + ")");
        }

        // 4. Le protocole du type de l'alerte doit exister
        ProtocolDto protocole = protocolRequest.getProtocolByType(alerte.type());

        // 5. Le protocole doit correspondre au type de l'alerte
        if (protocole.type() == null || !protocole.type().equalsIgnoreCase(alerte.type())) {
            throw new TransitionInterditeException(
                    "Le protocole " + protocole.id() + " ne correspond pas au type d'alerte " + alerte.type());
        }

        // 6. Tout est valide : creation
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
    public Intervention modifierIntervention(Long id, String assistantNom) {
        Intervention intervention = getInterventionById(id);
        if (intervention.getStatut() == StatutIntervention.CLOTUREE) {
            throw new TransitionInterditeException("Une intervention CLOTUREE ne peut plus etre modifiee");
        }
        intervention.setAssistantNom(assistantNom);
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