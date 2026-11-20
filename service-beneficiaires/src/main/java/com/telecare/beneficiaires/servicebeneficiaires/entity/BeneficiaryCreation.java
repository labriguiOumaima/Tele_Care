package com.telecare.beneficiaires.servicebeneficiaires.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "beneficiary_creations")
public class BeneficiaryCreation {

    @Id
    @Column(length = 100)
    private String idempotencyKey;

    @Column(nullable = false, length = 64)
    private String requestHash;

    @Lob
    @Column(nullable = false)
    private String responseJson;
    @Version
    private Long version;

    protected BeneficiaryCreation() {
    }

    public BeneficiaryCreation(
            String idempotencyKey,
            String requestHash,
            String responseJson) {

        this.idempotencyKey = idempotencyKey;
        this.requestHash = requestHash;
        this.responseJson = responseJson;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public String getRequestHash() {
        return requestHash;
    }

    public String getResponseJson() {
        return responseJson;
    }
}