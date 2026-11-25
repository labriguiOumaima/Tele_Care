package com.assistance.alertes.event;
import com.assistance.alertes.dto.event.AlertTriggeredEvent;
import com.assistance.alertes.entity.Alert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import java.time.LocalDateTime;
@Component
public class AlertEventPublisher {
 private static final Logger log=LoggerFactory.getLogger(AlertEventPublisher.class);
 private final KafkaTemplate<String,Object> kafka;
 public AlertEventPublisher(KafkaTemplate<String,Object> kafka){this.kafka=kafka;}
 public void publishAlertTriggered(Alert a){
  var event=AlertTriggeredEvent.builder().alertId(a.getId()).beneficiaryId(a.getBeneficiaryId()).type(a.getType()).gravite(a.getGravite()).dateDeclenchement(a.getDateDeclenchement()).timestamp(LocalDateTime.now()).build();
  Runnable send=()->kafka.send("alert-triggered",a.getId().toString(),event).whenComplete((ok,error)->{if(error!=null)log.error("Publication alert-triggered echouee, alertId={}",a.getId(),error);else log.info("Kafka a confirme alert-triggered, alertId={}",a.getId());});
  if(TransactionSynchronizationManager.isSynchronizationActive())TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization(){public void afterCommit(){send.run();}});else send.run();
 }
}
