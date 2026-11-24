package com.telecare.utilisateurs.service;

import com.telecare.utilisateurs.dto.UtilisateurPageDto;
import com.telecare.utilisateurs.dto.UtilisateurRequestDto;
import com.telecare.utilisateurs.dto.UtilisateurResponseDto;
import com.telecare.utilisateurs.dto.VerificationUtilisateurDTO;
import com.telecare.utilisateurs.entity.Utilisateur;
import com.telecare.utilisateurs.exception.UtilisateurDejaExistantException;
import com.telecare.utilisateurs.mapper.UtilisateurMapper;
import com.telecare.utilisateurs.repository.UtilisateurRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;
    private final UtilisateurMapper utilisateurMapper;

    public UtilisateurService(UtilisateurRepository utilisateurRepository,
                              UtilisateurMapper utilisateurMapper) {
        this.utilisateurRepository = utilisateurRepository;
        this.utilisateurMapper = utilisateurMapper;
    }

    // ─── Jalon V1 : CRUD avec DTOs ───────────────────────────────────────────

    @Transactional
    public UtilisateurResponseDto ajouter(UtilisateurRequestDto requestDto) {
        if (utilisateurRepository.existsByEmail(requestDto.getEmail())) {
            throw new UtilisateurDejaExistantException(requestDto.getEmail());
        }

        Utilisateur utilisateur = utilisateurMapper.toEntity(requestDto);
        utilisateur.setActif(true);

        Utilisateur enregistre = utilisateurRepository.save(utilisateur);
        return utilisateurMapper.toDto(enregistre);
    }

    @Transactional(readOnly = true)
    public UtilisateurPageDto afficherTousPagine(int page, int taille) {
        var pageable = PageRequest.of(page, taille, Sort.by("id").ascending());
        var pageEntites = utilisateurRepository.findAll(pageable);
        var pageDtos = pageEntites.map(utilisateurMapper::toDto);
        return UtilisateurPageDto.from(pageDtos);
    }

    @Transactional(readOnly = true)
    public List<UtilisateurResponseDto> afficherTous() {
        return utilisateurRepository.findAll().stream()
                .map(utilisateurMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public UtilisateurResponseDto chercherParId(Long id) {
        Utilisateur utilisateur = getEntityById(id);
        return utilisateurMapper.toDto(utilisateur);
    }

    @Transactional
    public UtilisateurResponseDto modifier(Long id, UtilisateurRequestDto requestDto) {
        Utilisateur utilisateur = getEntityById(id);

        if (!utilisateur.getEmail().equalsIgnoreCase(requestDto.getEmail())
                && utilisateurRepository.existsByEmail(requestDto.getEmail())) {
            throw new UtilisateurDejaExistantException(requestDto.getEmail());
        }

        utilisateur.setNom(requestDto.getNom());
        utilisateur.setEmail(requestDto.getEmail());
        utilisateur.setRole(requestDto.getRole());

        return utilisateurMapper.toDto(utilisateurRepository.save(utilisateur));
    }

    @Transactional
    public void supprimer(Long id) {
        Utilisateur utilisateur = getEntityById(id);
        utilisateurRepository.delete(utilisateur);
    }

    @Transactional
    public UtilisateurResponseDto changerEtat(Long id, boolean actif) {
        Utilisateur utilisateur = getEntityById(id);
        utilisateur.setActif(actif);
        return utilisateurMapper.toDto(utilisateurRepository.save(utilisateur));
    }

    // ─── Jalon V4 : Vérification inter-service (Feign) ───────────────────────

    @Transactional(readOnly = true)
    public VerificationUtilisateurDTO verifierUtilisateur(Long id, String roleAttendu) {
        VerificationUtilisateurDTO verification = new VerificationUtilisateurDTO();
        verification.setUtilisateurId(id);

        utilisateurRepository.findById(id).ifPresentOrElse(utilisateur -> {
            verification.setExiste(true);
            verification.setActif(utilisateur.isActif());
            verification.setRole(utilisateur.getRole());
            verification.setRoleValide(utilisateur.getRole().equalsIgnoreCase(roleAttendu));
            verification.setMessage("Utilisateur trouve");
        }, () -> verification.setMessage("Utilisateur introuvable"));

        return verification;
    }

    private Utilisateur getEntityById(Long id) {
        return utilisateurRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable avec l'ID : " + id));
    }
}
