package com.telecare.serviceinterventions.feignClient;

import com.telecare.serviceinterventions.exception.ServiceExterneIndisponibleException;
import feign.FeignException;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

@Service
public class ResilientRequests {

    private static final Logger log = LoggerFactory.getLogger(ResilientRequests.class);

    private final AlerteRequest alerteRequest;
    private final ProtocolRequest protocolRequest;
    private final CacheManager cacheManager;

    public ResilientRequests(AlerteRequest alerteRequest,
                             ProtocolRequest protocolRequest,
                             CacheManager cacheManager) {
        this.alerteRequest = alerteRequest;
        this.protocolRequest = protocolRequest;
        this.cacheManager = cacheManager;
    }

    // --- Alerte : lecture tolerante (cache ou objet vide) ---
    @Retry(name = "alertes-service", fallbackMethod = "alerteDeRepli")
    @CircuitBreaker(name = "alertes-service")
    @Bulkhead(name = "alertes-service")
    public AlerteDto getAlerteByIdFb(Long id) {
        AlerteDto alerte = alerteRequest.getAlerteById(id);
        mettreEnCache("alertes", id, alerte);
        log.info("Alerte {} put in cache", id);
        return alerte;
    }

    // --- Alerte : lecture stricte pour la creation (exception 503) ---
    @Retry(name = "alertes-service", fallbackMethod = "alerteIndisponible")
    @CircuitBreaker(name = "alertes-service")
    @Bulkhead(name = "alertes-service")
    public AlerteDto getAlerteByIdStrict(Long id) {
        AlerteDto alerte = alerteRequest.getAlerteById(id);
        mettreEnCache("alertes", id, alerte);
        log.info("Alerte {} put in cache", id);
        return alerte;
    }

    // --- Protocole : lecture tolerante (null si indisponible) ---
    @Retry(name = "protocole-service", fallbackMethod = "protocoleDeRepli")
    @CircuitBreaker(name = "protocole-service")
    @Bulkhead(name = "protocole-service")
    public ProtocolDto getProtocolByTypeFb(String type) {
        return protocolRequest.getProtocolByType(type);
    }

    // --- Protocole : lecture stricte pour la creation (exception 503) ---
    @Retry(name = "protocole-service", fallbackMethod = "protocoleIndisponible")
    @CircuitBreaker(name = "protocole-service")
    @Bulkhead(name = "protocole-service")
    public ProtocolDto getProtocolByTypeStrict(String type) {
        return protocolRequest.getProtocolByType(type);
    }

    // --- Replis ---
    private AlerteDto alerteIndisponible(Long id, RuntimeException ex) {
        if (!estIndisponible(ex)) {
            throw ex;
        }
        throw new ServiceExterneIndisponibleException(
                "Service-Alertes est momentanément indisponible. Réessayez dans quelques instants.");
    }

    private AlerteDto alerteDeRepli(Long id, RuntimeException ex) {
        if (!estIndisponible(ex)) {
            throw ex;
        }
        AlerteDto enCache = lireCache("alertes", id, AlerteDto.class);
        log.info("Alerte {} loaded from cache OR empty", id);
        return enCache != null
                ? enCache
                : new AlerteDto(id, null, null, null, null, null);
    }

    private ProtocolDto protocoleIndisponible(String type, RuntimeException ex) {
        if (!estIndisponible(ex)) {
            throw ex;
        }
        throw new ServiceExterneIndisponibleException(
                "Le protocole est momentanément indisponible. Réessayez dans quelques instants.");
    }

    private ProtocolDto protocoleDeRepli(String type, RuntimeException ex) {
        if (!estIndisponible(ex)) {
            throw ex;
        }
        log.info("Protocole {} indisponible : reponse sans protocole", type);
        return null;
    }

    private boolean estIndisponible(RuntimeException cause) {
        return cause instanceof CallNotPermittedException
                || (cause instanceof FeignException feign
                && (feign.status() == -1 || feign.status() == 503));
    }

    // --- Cache ---
    private void mettreEnCache(String nomCache, Object cle, Object valeur) {
        Cache cache = cacheManager.getCache(nomCache);
        if (cache != null) {
            cache.put(cle, valeur);
        }
    }

    private <T> T lireCache(String nomCache, Object cle, Class<T> type) {
        Cache cache = cacheManager.getCache(nomCache);
        return cache != null ? cache.get(cle, type) : null;
    }
}