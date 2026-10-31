package com.telecare.beneficiaires.servicebeneficiaires.controller;

import com.telecare.beneficiaires.servicebeneficiaires.dto.ContactRequest;
import com.telecare.beneficiaires.servicebeneficiaires.dto.ContactResponse;
import com.telecare.beneficiaires.servicebeneficiaires.service.ContactService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
public class ContactController {

    private final ContactService contactService;

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    @PostMapping("/contacts")
    public ResponseEntity<ContactResponse> create(
            @Valid @RequestBody ContactRequest request) {

        ContactResponse response = contactService.create(request);

        return ResponseEntity
                .created(URI.create("/contacts/" + response.id()))
                .body(response);
    }

    @GetMapping("/contacts/{id}")
    public ContactResponse findById(@PathVariable Long id) {
        return contactService.findById(id);
    }

    @GetMapping("/beneficiaires/{beneficiaryId}/contacts")
    public List<ContactResponse> findByBeneficiaryId(
            @PathVariable Long beneficiaryId) {

        return contactService.findByBeneficiaryId(beneficiaryId);
    }

    @PutMapping("/contacts/{id}")
    public ContactResponse update(
            @PathVariable Long id,
            @Valid @RequestBody ContactRequest request) {

        return contactService.update(id, request);
    }

    @DeleteMapping("/contacts/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        contactService.delete(id);

        return ResponseEntity.noContent().build();
    }
}