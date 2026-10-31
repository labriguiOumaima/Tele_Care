package com.telecare.beneficiaires.servicebeneficiaires.controller;

import com.telecare.beneficiaires.servicebeneficiaires.dto.BeneficiaryRequest;
import com.telecare.beneficiaires.servicebeneficiaires.dto.BeneficiaryResponse;
import com.telecare.beneficiaires.servicebeneficiaires.dto.BeneficiaryDossierResponse;
import com.telecare.beneficiaires.servicebeneficiaires.service.BeneficiaryService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/beneficiaires")
public class BeneficiaryController {

    private final BeneficiaryService beneficiaryService;

    public BeneficiaryController(BeneficiaryService beneficiaryService) {
        this.beneficiaryService = beneficiaryService;
    }

    @PostMapping
    public ResponseEntity<BeneficiaryResponse> create(
            @Valid @RequestBody BeneficiaryRequest request) {

        BeneficiaryResponse response = beneficiaryService.create(request);

        return ResponseEntity
                .created(URI.create("/beneficiaires/" + response.id()))
                .body(response);
    }

    @GetMapping
    public List<BeneficiaryResponse> findAll() {
        return beneficiaryService.findAll();
    }

    @GetMapping("/{id}")
    public BeneficiaryResponse findById(@PathVariable Long id) {
        return beneficiaryService.findById(id);
    }

    @GetMapping("/{id}/dossier")
    public BeneficiaryDossierResponse getDossier(@PathVariable Long id) {
        return beneficiaryService.getDossier(id);
    }

    @PutMapping("/{id}")
    public BeneficiaryResponse update(
            @PathVariable Long id,
            @Valid @RequestBody BeneficiaryRequest request) {

        return beneficiaryService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        beneficiaryService.delete(id);

        return ResponseEntity.noContent().build();
    }
}