package com.telecare.serviceinterventions.feignClient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "service-alertes", contextId = "protocolRequest")
public interface ProtocolRequest {

    @GetMapping("/api/protocols/type/{type}")
    ProtocolDto getProtocolByType(@PathVariable("type") String type);
}