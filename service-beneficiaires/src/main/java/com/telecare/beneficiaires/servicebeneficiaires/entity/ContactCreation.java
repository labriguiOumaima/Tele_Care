package com.telecare.beneficiaires.servicebeneficiaires.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "contact_creations")
public class ContactCreation {

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

    protected ContactCreation() {
    }

    public ContactCreation(
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