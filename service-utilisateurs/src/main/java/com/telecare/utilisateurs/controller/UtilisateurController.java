package com.telecare.utilisateurs.controller;

import com.telecare.utilisateurs.dto.UtilisateurPageDto;
import com.telecare.utilisateurs.dto.UtilisateurRequestDto;
import com.telecare.utilisateurs.dto.UtilisateurResponseDto;
import com.telecare.utilisateurs.dto.VerificationUtilisateurDTO;
import com.telecare.utilisateurs.service.UtilisateurService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

/**
 * Contrôleur REST pour la gestion des utilisateurs.
 * Utilise strictement des DTOs pour découpler l'API du modèle de persistance (Jalon V1).
 */
@RestController
@RequestMapping("/api/utilisateurs")
public class UtilisateurController {

    private final UtilisateurService utilisateurService;

    public UtilisateurController(UtilisateurService utilisateurService) {
        this.utilisateurService = utilisateurService;
    }

    /**
     * Création d'un nouvel utilisateur (ex: Assistant).
     */
    @PostMapping
    public ResponseEntity<UtilisateurResponseDto> ajouter(@Valid @RequestBody UtilisateurRequestDto requestDto) {
        UtilisateurResponseDto cree = utilisateurService.ajouter(requestDto);
        return ResponseEntity
                .created(URI.create("/api/utilisateurs/" + cree.getId()))
                .body(cree);
    }

    @GetMapping("/assistants-actifs")
    public java.util.List<UtilisateurResponseDto> assistantsActifs() {
        return utilisateurService.afficherTous().stream()
                .filter(u -> u.isActif() && "ASSISTANT".equalsIgnoreCase(u.getRole()))
                .toList();
    }

    /**
     * Consultation paginée des utilisateurs.
     */
    @GetMapping
    public ResponseEntity<UtilisateurPageDto> afficherTous(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int taille) {

        return ResponseEntity.ok(utilisateurService.afficherTousPagine(page, taille));
    }

    /**
     * Consultation d'un utilisateur par son ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<UtilisateurResponseDto> chercherParId(@PathVariable Long id) {
        return ResponseEntity.ok(utilisateurService.chercherParId(id));
    }

    /**
     * Modification d'un utilisateur existant.
     */
    @PutMapping("/{id}")
    public ResponseEntity<UtilisateurResponseDto> modifier(
            @PathVariable Long id,
            @Valid @RequestBody UtilisateurRequestDto requestDto) {

        return ResponseEntity.ok(utilisateurService.modifier(id, requestDto));
    }

    /**
     * Suppression d'un utilisateur.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        utilisateurService.supprimer(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Activation / Désactivation de l'utilisateur.
     */
    @PatchMapping("/{id}/actif")
    public ResponseEntity<UtilisateurResponseDto> changerEtat(
            @PathVariable Long id,
            @RequestParam boolean actif) {

        return ResponseEntity.ok(utilisateurService.changerEtat(id, actif));
    }

    /**
     * Vérification de l'utilisateur et de son rôle (consommé par service-interventions via Feign).
     */
    @GetMapping("/{id}/verification")
    public ResponseEntity<VerificationUtilisateurDTO> verifierUtilisateur(
            @PathVariable Long id,
            @RequestParam String role) {

        return ResponseEntity.ok(utilisateurService.verifierUtilisateur(id, role));
    }
}
