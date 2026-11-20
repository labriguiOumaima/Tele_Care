package com.telecare.servicenotification.feignClient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "service-beneficiaires")
public interface ContactRequest {

    @GetMapping("/beneficiaires/{beneficiaryId}/contacts")
    List<ContactDto> getContacts(@PathVariable("beneficiaryId") Long beneficiaryId);
}