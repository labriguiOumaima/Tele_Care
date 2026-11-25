package com.telecare.beneficiaires.servicebeneficiaires.service;

import com.telecare.beneficiaires.servicebeneficiaires.dto.ContactRequest;
import com.telecare.beneficiaires.servicebeneficiaires.dto.ContactResponse;
import com.telecare.beneficiaires.servicebeneficiaires.entity.Contact;
import com.telecare.beneficiaires.servicebeneficiaires.entity.ContactCreation;
import com.telecare.beneficiaires.servicebeneficiaires.repository.BeneficiaryRepository;
import com.telecare.beneficiaires.servicebeneficiaires.repository.ContactRepository;
import com.telecare.beneficiaires.servicebeneficiaires.repository.ContactCreationRepository;

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
public class ContactService {

    private final ContactRepository contactRepository;
    private final BeneficiaryRepository beneficiaryRepository;
    private final ContactCreationRepository creationRepository;

    private final JsonCodec jsonMapper = new JsonCodec();

    public ContactService(
            ContactRepository contactRepository,
            BeneficiaryRepository beneficiaryRepository,
            ContactCreationRepository creationRepository) {

        this.contactRepository = contactRepository;
        this.beneficiaryRepository = beneficiaryRepository;
        this.creationRepository = creationRepository;
    }

    // Création simple, utilisée par le contrôleur actuel.
    public ContactResponse create(ContactRequest request) {
        checkBeneficiaryExists(request.beneficiaryId());

        Contact contact = new Contact();
        applyRequest(contact, request);

        return toResponse(contactRepository.save(contact));
    }

    // Création avec une clé d'idempotence.
    public ContactResponse create(
            ContactRequest request,
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
            ContactCreation previous = previousCreation.get();

            if (!previous.getRequestHash().equals(requestHash)) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Cette clé a déjà été utilisée avec des données différentes"
                );
            }

            return jsonMapper.readValue(
                    previous.getResponseJson(),
                    ContactResponse.class
            );
        }

        ContactResponse response = create(request);

        creationRepository.saveAndFlush(new ContactCreation(
                idempotencyKey,
                requestHash,
                jsonMapper.writeValueAsString(response)
        ));

        return response;
    }

    @Transactional(readOnly = true)
    public ContactResponse findById(Long id) {
        return toResponse(getExistingContact(id));
    }

    @Transactional(readOnly = true)
    public List<ContactResponse> findByBeneficiaryId(Long beneficiaryId) {
        checkBeneficiaryExists(beneficiaryId);

        return contactRepository.findByBeneficiaryId(beneficiaryId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public ContactResponse update(Long id, ContactRequest request) {
        Contact contact = getExistingContact(id);

        checkBeneficiaryExists(request.beneficiaryId());

        applyRequest(contact, request);

        return toResponse(contactRepository.save(contact));
    }

    public void delete(Long id) {
        Contact contact = getExistingContact(id);
        contactRepository.delete(contact);
    }

    private void checkBeneficiaryExists(Long beneficiaryId) {
        if (!beneficiaryRepository.existsById(beneficiaryId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Bénéficiaire introuvable : " + beneficiaryId
            );
        }
    }

    private Contact getExistingContact(Long id) {
        return contactRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Contact introuvable : " + id
                ));
    }

    private void applyRequest(Contact contact, ContactRequest request) {
        contact.setBeneficiaryId(request.beneficiaryId());
        contact.setNom(request.nom());
        contact.setLien(request.lien());
        contact.setTelephone(request.telephone());
        contact.setEmail(request.email());
    }

    private ContactResponse toResponse(Contact contact) {
        return new ContactResponse(
                contact.getId(),
                contact.getBeneficiaryId(),
                contact.getNom(),
                contact.getLien(),
                contact.getTelephone(),
                contact.getEmail()
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