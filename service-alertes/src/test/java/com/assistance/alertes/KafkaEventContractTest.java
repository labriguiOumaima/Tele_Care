package com.assistance.alertes;
import com.assistance.alertes.dto.event.AlertTriggeredEvent;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.support.serializer.JsonSerializer;
import static org.junit.jupiter.api.Assertions.*;
class KafkaEventContractTest {
 @Test void datesArePortableIsoStrings() throws Exception {
  var date=java.time.LocalDateTime.of(2026,11,25,10,30);
  var event=AlertTriggeredEvent.builder().alertId(1L).beneficiaryId(2L).type("CHUTE").gravite("ELEVEE").dateDeclenchement(date).timestamp(date).build();
  try(var serializer=new JsonSerializer<AlertTriggeredEvent>()){
   var bytes=serializer.serialize("alert-triggered",event);
   var json=new com.fasterxml.jackson.databind.ObjectMapper().readTree(bytes);
   assertTrue(json.get("dateDeclenchement").isTextual());assertEquals("2026-11-25T10:30:00",json.get("dateDeclenchement").asText());assertTrue(json.get("timestamp").isTextual());
  }
 }
}
