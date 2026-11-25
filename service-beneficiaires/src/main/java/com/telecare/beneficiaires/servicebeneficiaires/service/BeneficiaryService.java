package com.telecare.beneficiaires.servicebeneficiaires.service;

import com.telecare.beneficiaires.servicebeneficiaires.dto.BeneficiaryRequest;
import com.telecare.beneficiaires.servicebeneficiaires.dto.BeneficiaryResponse;
import com.telecare.beneficiaires.servicebeneficiaires.dto.BeneficiaryDossierResponse;
import com.telecare.beneficiaires.servicebeneficiaires.dto.ContactResponse;
import com.telecare.beneficiaires.servicebeneficiaires.entity.Beneficiary;
import com.telecare.beneficiaires.servicebeneficiaires.entity.BeneficiaryCreation;
import com.telecare.beneficiaires.servicebeneficiaires.repository.BeneficiaryRepository;
import com.telecare.beneficiaires.servicebeneficiaires.repository.ContactRepository;
import com.telecare.beneficiaires.servicebeneficiaires.repository.BeneficiaryCreationRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.telecare.beneficiaires.servicebeneficiaires.util.JsonCodec;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;

@Service
@Transactional
public class BeneficiaryService {

    private final BeneficiaryRepository beneficiaryRepository;
    private final ContactRepository contactRepository;
    private final BeneficiaryCreationRepository creationRepository;

    private final JsonCodec jsonMapper = new JsonCodec();

    public BeneficiaryService(
            BeneficiaryRepository beneficiaryRepository,
            ContactRepository contactRepository,
            BeneficiaryCreationRepository creationRepository) {

        this.beneficiaryRepository = beneficiaryRepository;
        this.contactRepository = contactRepository;
        this.creationRepository = creationRepository;
    }

    // Création simple, conservée pour le contrôleur actuel.
    public BeneficiaryResponse create(BeneficiaryRequest request) {
        Beneficiary beneficiary = new Beneficiary();
        applyRequest(beneficiary, request);

        Beneficiary saved = beneficiaryRepository.save(beneficiary);

        return toResponse(saved);
    }

    // Création avec une clé d'idempotence.
    public BeneficiaryResponse create(
            BeneficiaryRequest request,
            String idempotencyKey) {

        if (idempotencyKey == null
                || idempotencyKey.isBlank()
                || idempotencyKey.length() > 100) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Idempotency-Key doit contenir entre 1 et 100 caractères"
            );
        }

        String requestJson = jsonMapper.writeValueAsString(request);
        String requestHash = calculateHash(requestJson);

        var previousCreation =
                creationRepository.findById(idempotencyKey);

        if (previousCreation.isPresent()) {
            BeneficiaryCreation previous = previousCreation.get();

            if (!previous.getRequestHash().equals(requestHash)) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Cette clé a déjà été utilisée avec des données différentes"
                );
            }

            return jsonMapper.readValue(
                    previous.getResponseJson(),
                    BeneficiaryResponse.class
            );
        }

        BeneficiaryResponse response = create(request);

        creationRepository.saveAndFlush(new BeneficiaryCreation(
                idempotencyKey,
                requestHash,
                jsonMapper.writeValueAsString(response)
        ));

        return response;
    }

    // Consulter tous les bénéficiaires.
    @Transactional(readOnly = true)
    public List<BeneficiaryResponse> findAll() {
        return beneficiaryRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // Consulter un bénéficiaire.
    @Transactional(readOnly = true)
    public BeneficiaryResponse findById(Long id) {
        return toResponse(getExistingBeneficiary(id));
    }

    // Consulter le bénéficiaire et ses contacts.
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
                                contact.getTelephone(),
                                contact.getEmail()
                        ))
                        .toList();

        return new BeneficiaryDossierResponse(
                toResponse(beneficiary),
                contacts
        );
    }

    // Modifier un bénéficiaire.
    public BeneficiaryResponse update(
            Long id,
            BeneficiaryRequest request) {

        Beneficiary beneficiary = getExistingBeneficiary(id);
        applyRequest(beneficiary, request);

        return toResponse(beneficiaryRepository.save(beneficiary));
    }

    // Supprimer un bénéficiaire sans contacts.
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

    private String calculateHash(String value) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(hash);

        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(
                    "SHA-256 indisponible",
                    ex
            );
        }
    }
}