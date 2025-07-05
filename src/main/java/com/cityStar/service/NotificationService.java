package com.cityStar.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.cityStar.dto.NotificationDTO;
import com.cityStar.model.Notification;
import com.cityStar.model.User;
import com.cityStar.repository.InotificationRepository;
import com.cityStar.rowmapper.NotificationRowMapper;

@Service
public class NotificationService {

    private final InotificationRepository notificationRepository;
    public NotificationService(InotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }
    
    public void createNotification(User user, String message) {
        Notification notification = Notification.builder()
                .user(user)
                .message(message)
                .isRead(false)
                .build();
        notificationRepository.save(notification);
    }

    public List<NotificationDTO> getNotificationsByUser(User user) {
        return notificationRepository.findByUser(user)
                .stream()
                .map(NotificationRowMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<NotificationDTO> getUnreadNotifications(User user) {
        return notificationRepository.findByUserAndIsReadFalse(user)
                .stream()
                .map(NotificationRowMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<NotificationDTO> getAllNotifications() {
            return notificationRepository.findAll()
            .stream()
            .map(NotificationRowMapper::toDto)
            .collect(Collectors.toList());
    }

    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            notification.setRead(true);
            notificationRepository.save(notification);
        });
    }
}
