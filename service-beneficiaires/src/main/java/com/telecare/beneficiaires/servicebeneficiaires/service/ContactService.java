package com.telecare.beneficiaires.servicebeneficiaires.service;

import com.telecare.beneficiaires.servicebeneficiaires.dto.ContactRequest;
import com.telecare.beneficiaires.servicebeneficiaires.dto.ContactResponse;
import com.telecare.beneficiaires.servicebeneficiaires.entity.Contact;
import com.telecare.beneficiaires.servicebeneficiaires.repository.BeneficiaryRepository;
import com.telecare.beneficiaires.servicebeneficiaires.repository.ContactRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class ContactService {

    private final ContactRepository contactRepository;
    private final BeneficiaryRepository beneficiaryRepository;

    public ContactService(
            ContactRepository contactRepository,
            BeneficiaryRepository beneficiaryRepository) {

        this.contactRepository = contactRepository;
        this.beneficiaryRepository = beneficiaryRepository;
    }

    public ContactResponse create(ContactRequest request) {
        checkBeneficiaryExists(request.beneficiaryId());

        Contact contact = new Contact();
        applyRequest(contact, request);

        return toResponse(contactRepository.save(contact));
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

        // Vérifier le bénéficiaire avant toute modification
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
    }

    private ContactResponse toResponse(Contact contact) {
        return new ContactResponse(
                contact.getId(),
                contact.getBeneficiaryId(),
                contact.getNom(),
                contact.getLien(),
                contact.getTelephone()
        );
    }
}