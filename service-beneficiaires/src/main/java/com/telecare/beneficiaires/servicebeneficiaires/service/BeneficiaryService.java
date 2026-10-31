package com.telecare.beneficiaires.servicebeneficiaires.service;

import com.telecare.beneficiaires.servicebeneficiaires.dto.BeneficiaryRequest;
import com.telecare.beneficiaires.servicebeneficiaires.dto.BeneficiaryResponse;
import com.telecare.beneficiaires.servicebeneficiaires.dto.BeneficiaryDossierResponse;
import com.telecare.beneficiaires.servicebeneficiaires.dto.ContactResponse;
import com.telecare.beneficiaires.servicebeneficiaires.entity.Beneficiary;
import com.telecare.beneficiaires.servicebeneficiaires.repository.BeneficiaryRepository;
import com.telecare.beneficiaires.servicebeneficiaires.repository.ContactRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class BeneficiaryService {

    private final BeneficiaryRepository beneficiaryRepository;
    private final ContactRepository contactRepository;

    public BeneficiaryService(
            BeneficiaryRepository beneficiaryRepository,
            ContactRepository contactRepository) {

        this.beneficiaryRepository = beneficiaryRepository;
        this.contactRepository = contactRepository;
    }

    // Ajouter un bénéficiaire
    public BeneficiaryResponse create(BeneficiaryRequest request) {
        Beneficiary beneficiary = new Beneficiary();
        applyRequest(beneficiary, request);

        Beneficiary saved = beneficiaryRepository.save(beneficiary);

        return toResponse(saved);
    }

    // Consulter tous les bénéficiaires
    @Transactional(readOnly = true)
    public List<BeneficiaryResponse> findAll() {
        return beneficiaryRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // Consulter un bénéficiaire
    @Transactional(readOnly = true)
    public BeneficiaryResponse findById(Long id) {
        return toResponse(getExistingBeneficiary(id));
    }

    // Consulter le dossier complet : bénéficiaire et contacts
    @Transactional(readOnly = true)
    public BeneficiaryDossierResponse getDossier(Long id) {
        Beneficiary beneficiary = getExistingBeneficiary(id);

        List<ContactResponse> contacts =
                contactRepository.findByBeneficiaryId(id)
                        .stream()
                        .map(contact -> new ContactResponse(
                                contact.getId(),
                                contact.getBeneficiaryId(),
                                contact.getNom(),
                                contact.getLien(),
                                contact.getTelephone()
                        ))
                        .toList();

        return new BeneficiaryDossierResponse(
                toResponse(beneficiary),
                contacts
        );
    }

    // Modifier un bénéficiaire existant
    public BeneficiaryResponse update(
            Long id,
            BeneficiaryRequest request) {

        Beneficiary beneficiary = getExistingBeneficiary(id);
        applyRequest(beneficiary, request);

        return toResponse(beneficiaryRepository.save(beneficiary));
    }

    // Supprimer un bénéficiaire sans contacts
    public void delete(Long id) {
        Beneficiary beneficiary = getExistingBeneficiary(id);

        if (contactRepository.existsByBeneficiaryId(id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Suppression impossible : ce bénéficiaire possède des contacts"
            );
        }

        beneficiaryRepository.delete(beneficiary);
    }

    private Beneficiary getExistingBeneficiary(Long id) {
        return beneficiaryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Bénéficiaire introuvable : " + id
                ));
    }

    private void applyRequest(
            Beneficiary beneficiary,
            BeneficiaryRequest request) {

        beneficiary.setNom(request.nom());
        beneficiary.setAge(request.age());
        beneficiary.setAdresse(request.adresse());
        beneficiary.setPathologie(request.pathologie());
        beneficiary.setTelephone(request.telephone());
    }

    private BeneficiaryResponse toResponse(Beneficiary beneficiary) {
        return new BeneficiaryResponse(
                beneficiary.getId(),
                beneficiary.getNom(),
                beneficiary.getAge(),
                beneficiary.getAdresse(),
                beneficiary.getPathologie(),
                beneficiary.getTelephone()
        );
    }
}