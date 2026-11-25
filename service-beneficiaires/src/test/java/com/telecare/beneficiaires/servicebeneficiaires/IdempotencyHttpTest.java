package com.telecare.beneficiaires.servicebeneficiaires;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest(properties={"spring.cloud.config.enabled=false","spring.config.import="}, webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class IdempotencyHttpTest {
 @Autowired TestRestTemplate http;
 HttpEntity<String> entity(String key,String name){var h=new HttpHeaders();h.setContentType(MediaType.APPLICATION_JSON);h.set("Idempotency-Key",key);return new HttpEntity<>("{\"nom\":\""+name+"\",\"age\":75,\"adresse\":\"Test\",\"telephone\":\"0600000000\",\"pathologie\":\"Test\"}",h);}
 @Test void replayAndConflict(){String key=UUID.randomUUID().toString();var a=http.postForEntity("/beneficiaires",entity(key,"Test"),String.class);var b=http.postForEntity("/beneficiaires",entity(key,"Test"),String.class);assertEquals(201,a.getStatusCode().value());assertEquals(a.getBody(),b.getBody());assertEquals("true",b.getHeaders().getFirst("Idempotency-Replayed"));assertEquals(409,http.postForEntity("/beneficiaires",entity(key,"Autre"),String.class).getStatusCode().value());}
 @Test void concurrentReplayHasSingleResult() throws Exception {String key=UUID.randomUUID().toString();var pool=Executors.newFixedThreadPool(3);try{var jobs=new ArrayList<Callable<ResponseEntity<String>>>();for(int i=0;i<3;i++)jobs.add(()->http.postForEntity("/beneficiaires",entity(key,"Concurrent"),String.class));var results=pool.invokeAll(jobs);String body=results.get(0).get().getBody();for(var f:results){assertEquals(201,f.get().getStatusCode().value());assertEquals(body,f.get().getBody());}}finally{pool.shutdownNow();}}
}
