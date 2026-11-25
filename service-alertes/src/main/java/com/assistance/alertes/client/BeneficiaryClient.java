package com.assistance.alertes.client;

import com.assistance.alertes.dto.feign.BeneficiaryDTO;
import com.assistance.alertes.dto.feign.ContactDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "service-beneficiaires", fallback = BeneficiaryClientFallback.class)
public interface BeneficiaryClient {

    @GetMapping("/api/beneficiaries/{id}")
    BeneficiaryDTO getBeneficiaryById(@PathVariable("id") Long id);

    @GetMapping("/api/beneficiaries/{id}/contacts")
    List<ContactDTO> getContactsByBeneficiaryId(@PathVariable("id") Long id);
}
