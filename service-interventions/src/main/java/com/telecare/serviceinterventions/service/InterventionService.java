package com.telecare.serviceinterventions.service;

import com.telecare.serviceinterventions.dto.InterventionDetailDto;
import com.telecare.serviceinterventions.entite.Intervention;
import com.telecare.serviceinterventions.entite.StatutIntervention;
import com.telecare.serviceinterventions.event.InterventionAssignedEvent;
import com.telecare.serviceinterventions.event.EmergencyResolvedEvent;
import com.telecare.serviceinterventions.exception.InterventionExistanteException;
import com.telecare.serviceinterventions.exception.RessourceIntrouvableException;
import com.telecare.serviceinterventions.exception.TransitionInterditeException;
import com.telecare.serviceinterventions.feignClient.AlerteDto;
import com.telecare.serviceinterventions.feignClient.ProtocolDto;
import com.telecare.serviceinterventions.feignClient.ResilientRequests;
import com.telecare.serviceinterventions.feignClient.UtilisateurDto; // NOUVEAU
import com.telecare.serviceinterventions.messaging.InterventionEventPublisher;
import com.telecare.serviceinterventions.repository.InterventionRepository;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class InterventionService {

    private final InterventionRepository interventionRepository;
    private final ResilientRequests resilientRequests;
    private final InterventionEventPublisher eventPublisher;

    public InterventionService(InterventionRepository interventionRepository,
                               ResilientRequests resilientRequests,
                               InterventionEventPublisher eventPublisher) {
        this.interventionRepository = interventionRepository;
        this.resilientRequests = resilientRequests;
        this.eventPublisher = eventPublisher;
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

    @RateLimiter(name = "consultation", fallbackMethod = "consultationLimitee")
    @Transactional(readOnly = true)
    public InterventionDetailDto getInterventionDetail(Long id) {
        Intervention intervention = getInterventionById(id);
        // Methode tolerante (cache ou objet vide) via ResilientRequests
        AlerteDto alerte = resilientRequests.getAlerteByIdFb(intervention.getAlertId());

        String typeAlerte = alerte != null ? alerte.type() : null;
        String graviteAlerte = alerte != null ? alerte.gravite() : null;
        String statutAlerte = alerte != null ? alerte.statut() : null;

        // Methode tolerante pour le protocole : null si le service est indisponible
        ProtocolDto protocole = typeAlerte != null
                ? resilientRequests.getProtocolByTypeFb(typeAlerte)
                : null;

        return new InterventionDetailDto(
                intervention.getId(),
                intervention.getAlertId(),
                intervention.getAssistantId(),
                intervention.getHeureArrivee(),
                intervention.getCompteRendu(),
                intervention.getStatut(),
                typeAlerte,
                graviteAlerte,
                statutAlerte,
                protocole != null ? protocole.etapes() : null,
                protocole != null ? protocole.niveauPriorite() : null);
    }

    // Repli du RateLimiter : donnees locales seulement, sans appel Feign
    public InterventionDetailDto consultationLimitee(Long id, RequestNotPermitted cause) {
        Intervention intervention = getInterventionById(id);
        return new InterventionDetailDto(
                intervention.getId(),
                intervention.getAlertId(),
                intervention.getAssistantId(),
                intervention.getHeureArrivee(),
                intervention.getCompteRendu(),
                intervention.getStatut(),
                null,   // typeAlerte
                null,   // graviteAlerte
                null,   // statutAlerte
                null,   // etapes du protocole
                null);  // niveauPriorite
    }

    @Transactional
    public Intervention creerIntervention(Intervention intervention) {
        // 1. Idempotence : l'alertId sert de cle. Verification AVANT tout appel externe.
        if (interventionRepository.existsByAlertId(intervention.getAlertId())) {
            throw new InterventionExistanteException(intervention.getAlertId());
        }

        // 2. L'alerte doit exister chez service-alertes (404 si introuvable, 503 si injoignable)
        AlerteDto alerte = resilientRequests.getAlerteByIdStrict(intervention.getAlertId());

        // 3. L'alerte doit etre DECLENCHEE
        if (!"DECLENCHEE".equals(alerte.statut())) {
            throw new TransitionInterditeException(
                    "L'alerte " + alerte.id() + " n'est pas DECLENCHEE (statut actuel : " + alerte.statut() + ")");
        }

        // 4. Le protocole du type de l'alerte doit exister (404 si introuvable, 503 si injoignable)
        ProtocolDto protocole = resilientRequests.getProtocolByTypeStrict(alerte.type());

        // 5. Le protocole doit correspondre au type de l'alerte
        if (protocole.type() == null || !protocole.type().equalsIgnoreCase(alerte.type())) {
            throw new TransitionInterditeException(
                    "Le protocole " + protocole.id() + " ne correspond pas au type d'alerte " + alerte.type());
        }

        // 5 bis. L'assistant doit etre actif et avoir le role ASSISTANT (V4)   // NOUVEAU
        verifierAssistant(intervention.getAssistantId());                        // NOUVEAU

        // 6. Tout est valide : creation
        intervention.setId(null);
        intervention.setBeneficiaryId(alerte.beneficiaryId());
        intervention.setStatut(StatutIntervention.AFFECTEE);
        Intervention enregistree = interventionRepository.save(intervention);

        // 7. Publication de intervention-assigned APRES le commit en base
        InterventionAssignedEvent event = new InterventionAssignedEvent(
                UUID.randomUUID().toString(),
                enregistree.getId(),
                enregistree.getAlertId(),
                enregistree.getBeneficiaryId(),
                enregistree.getAssistantId(),
                LocalDateTime.now());
        apresCommit(() -> eventPublisher.publierAffectation(event));

        return enregistree;
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
        Intervention enregistree = interventionRepository.save(intervention);

        // Publication de emergency-resolved APRES le commit en base
        EmergencyResolvedEvent event = new EmergencyResolvedEvent(
                UUID.randomUUID().toString(),
                enregistree.getId(),
                enregistree.getAlertId(),
                enregistree.getBeneficiaryId(),
                enregistree.getCompteRendu(),
                LocalDateTime.now());
        apresCommit(() -> eventPublisher.publierCloture(event));

        return enregistree;
    }

    @Transactional
    public Intervention modifierIntervention(Long id, Long assistantId) {
        Intervention intervention = getInterventionById(id);
        if (intervention.getStatut() == StatutIntervention.CLOTUREE) {
            throw new TransitionInterditeException("Une intervention CLOTUREE ne peut plus etre modifiee");
        }
        verifierAssistant(assistantId); // NOUVEAU
        intervention.setAssistantId(assistantId);
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

    // NOUVEAU : l'assistant doit exister, etre actif et avoir le role ASSISTANT
    private void verifierAssistant(Long assistantId) {
        // 404 si introuvable, 503 si service-utilisateurs injoignable
        UtilisateurDto assistant = resilientRequests.getUtilisateurByIdStrict(assistantId);

        if (!Boolean.TRUE.equals(assistant.actif())) {
            throw new TransitionInterditeException(
                    "L'assistant " + assistantId + " n'est pas actif");
        }
        if (!"ASSISTANT".equalsIgnoreCase(assistant.role())) {
            throw new TransitionInterditeException(
                    "L'utilisateur " + assistantId + " n'a pas le role ASSISTANT (role actuel : "
                            + assistant.role() + ")");
        }
    }

    // Execute l'action seulement si la transaction est validee (commit)
    private void apresCommit(Runnable action) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    action.run();
                }
            });
        } else {
            action.run();
        }
    }
}