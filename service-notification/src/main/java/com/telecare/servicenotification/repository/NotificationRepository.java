package com.telecare.servicenotification.repository;

import com.telecare.servicenotification.entite.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    boolean existsByEventId(String eventId);

    List<Notification> findByAlertId(Long alertId);
}