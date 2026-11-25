package com.assistance.alertes;
import com.assistance.alertes.client.*;
import com.assistance.alertes.dto.feign.BeneficiaryDTO;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest(properties={"spring.cloud.config.enabled=false","spring.config.import="})
@ActiveProfiles("test")
class ResilientBeneficiaryClientTest {
 @MockitoBean BeneficiaryClient remote;
 @Autowired ResilientBeneficiaryClient resilient;
 @Autowired io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry circuits;
 @BeforeEach void resetCircuit(){circuits.circuitBreaker("beneficiaires-service").reset();}
 private feign.Request request(){return feign.Request.create(feign.Request.HttpMethod.GET,"http://test/beneficiaires/1",java.util.Map.of(),null,java.nio.charset.StandardCharsets.UTF_8,null);}
 @Test void retriesUnavailableThenSucceeds(){
  var error=new feign.FeignException.ServiceUnavailable("down",request(),null,java.util.Map.of());
  when(remote.getBeneficiaryById(1L)).thenThrow(error).thenThrow(error).thenReturn(new BeneficiaryDTO(1L,"Test",70,"Adresse","","0600000000"));
  assertEquals(1L,resilient.getBeneficiaryById(1L).getId());verify(remote,times(3)).getBeneficiaryById(1L);
 }
 @Test void missingReferenceIsNotRetriedOrReplaced(){
  when(remote.getBeneficiaryById(999L)).thenThrow(new feign.FeignException.NotFound("missing",request(),null,java.util.Map.of()));
  assertThrows(feign.FeignException.NotFound.class,()->resilient.getBeneficiaryById(999L));verify(remote,times(1)).getBeneficiaryById(999L);
 }
 @Test void retriesContactLookupThenReturnsRealEmptyList(){
  var error=new feign.FeignException.ServiceUnavailable("down",request(),null,java.util.Map.of());
  when(remote.getContactsByBeneficiaryId(1L)).thenThrow(error).thenThrow(error).thenReturn(java.util.List.of());
  assertTrue(resilient.getContactsByBeneficiaryId(1L).isEmpty());verify(remote,times(3)).getContactsByBeneficiaryId(1L);
 }
 @Test void openCircuitBlocksRemoteCall(){
  circuits.circuitBreaker("beneficiaires-service").transitionToOpenState();
  assertThrows(io.github.resilience4j.circuitbreaker.CallNotPermittedException.class,()->resilient.getBeneficiaryById(1L));
  verifyNoInteractions(remote);
 }
}
