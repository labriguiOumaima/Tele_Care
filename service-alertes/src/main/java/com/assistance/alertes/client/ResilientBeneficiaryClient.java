package com.assistance.alertes.client;

import com.assistance.alertes.dto.feign.BeneficiaryDTO;
import com.assistance.alertes.dto.feign.ContactDTO;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ResilientBeneficiaryClient {

    private final BeneficiaryClient client;

    public ResilientBeneficiaryClient(BeneficiaryClient client) {
        this.client = client;
    }

    @Retry(name = "beneficiaires-service")
    @CircuitBreaker(name = "beneficiaires-service")
    public BeneficiaryDTO getBeneficiaryById(Long id) {
        return client.getBeneficiaryById(id);
    }

    @Retry(name = "beneficiaires-service")
    @CircuitBreaker(name = "beneficiaires-service")
    public List<ContactDTO> getContactsByBeneficiaryId(Long id) {
        return client.getContactsByBeneficiaryId(id);
    }
}