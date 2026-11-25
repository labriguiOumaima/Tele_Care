package com.telecare.servicenotification.service;
import com.telecare.servicenotification.repository.NotificationRepository;
import com.telecare.servicenotification.feignClient.*;
import com.telecare.servicenotification.event.InterventionAssignedEvent;
import org.junit.jupiter.api.*;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
class NotificationServiceTest {
 NotificationRepository repository;ResilientRequests contacts;JavaMailSender mail;NotificationService service;
 @BeforeEach void setup(){repository=mock(NotificationRepository.class);contacts=mock(ResilientRequests.class);mail=mock(JavaMailSender.class);service=new NotificationService(repository,contacts,mail);org.springframework.test.util.ReflectionTestUtils.setField(service,"from","telecare@localhost.test");}
 InterventionAssignedEvent event(){return new InterventionAssignedEvent("event-1",1L,2L,3L,4L,java.time.LocalDateTime.now());}
 @Test void sendsMailAndDeduplicates(){when(repository.existsByEventId("event-1")).thenReturn(false,true);when(contacts.getContacts(3L)).thenReturn(java.util.List.of(new ContactDto(5L,3L,"Famille","Fille","0600000000","family@example.test")));service.notifierPriseEnCharge(event());service.notifierPriseEnCharge(event());var capture=org.mockito.ArgumentCaptor.forClass(SimpleMailMessage.class);verify(mail,times(1)).send(capture.capture());assertEquals("family@example.test",capture.getValue().getTo()[0]);verify(repository,times(1)).save(any());}
 @Test void failedMailIsNotRecordedAsSent(){when(contacts.getContacts(3L)).thenReturn(java.util.List.of(new ContactDto(5L,3L,"Famille","Fille","0600000000","family@example.test")));doThrow(new org.springframework.mail.MailSendException("SMTP unavailable")).when(mail).send(any(SimpleMailMessage.class));assertThrows(org.springframework.mail.MailSendException.class,()->service.notifierPriseEnCharge(event()));verify(repository,never()).save(any());}
}
