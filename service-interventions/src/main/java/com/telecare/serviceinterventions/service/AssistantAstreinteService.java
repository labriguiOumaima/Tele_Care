package com.telecare.serviceinterventions.service;
import com.telecare.serviceinterventions.feignClient.UtilisateurRequest;
import org.springframework.stereotype.Service;
import java.util.concurrent.atomic.AtomicInteger;
@Service
public class AssistantAstreinteService {
 private final UtilisateurRequest utilisateurs;
 private final AtomicInteger compteur=new AtomicInteger();
 public AssistantAstreinteService(UtilisateurRequest utilisateurs){this.utilisateurs=utilisateurs;}
 public Long prochainAssistant(){
  var assistants=utilisateurs.getAssistantsActifs();
  if(assistants.isEmpty())throw new IllegalStateException("Aucun assistant actif disponible");
  return assistants.get(Math.floorMod(compteur.getAndIncrement(),assistants.size())).id();
 }
}
