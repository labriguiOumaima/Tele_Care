package com.telecare.servicenotification.feignClient;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ResilientRequests {

    private final ContactRequest contactRequest;

    public ResilientRequests(ContactRequest contactRequest) {
        this.contactRequest = contactRequest;
    }

    // Pas de valeur de repli : sans contact, on ne peut pas notifier.
    // L'exception remonte au consommateur Kafka, qui reessaie (voir KafkaConfig).
    @Retry(name = "beneficiaires-service")
    @CircuitBreaker(name = "beneficiaires-service")
    @Bulkhead(name = "beneficiaires-service")
    public List<ContactDto> getContacts(Long beneficiaryId) {
        return contactRequest.getContacts(beneficiaryId);
    }
}