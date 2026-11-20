package com.telecare.servicenotification.controller;

import com.telecare.servicenotification.dto.NotificationDto;
import com.telecare.servicenotification.service.NotificationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    // Preuve de l'envoi : liste des notifications, filtrable par alerte
    @GetMapping
    public List<NotificationDto> lister(@RequestParam(required = false) Long alertId) {
        return notificationService.lister(alertId).stream()
                .map(NotificationDto::from)
                .toList();
    }

    @GetMapping("/{id}")
    public NotificationDto getById(@PathVariable Long id) {
        return NotificationDto.from(notificationService.getById(id));
    }
}