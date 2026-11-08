package com.telecare.serviceinterventions.feignClient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "service-alertes", contextId = "alerteRequest")
public interface AlerteRequest {

    @GetMapping("/alertes/{id}")
    AlerteDto getAlerteById(@PathVariable("id") Long id);
}