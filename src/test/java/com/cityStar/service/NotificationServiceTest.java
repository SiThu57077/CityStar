package com.cityStar.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import com.cityStar.dto.NotificationDTO;
import com.cityStar.model.Notification;
import com.cityStar.model.User;
import com.cityStar.repository.InotificationRepository;
import com.cityStar.rowmapper.NotificationRowMapper;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {

    @Mock
    private InotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    private User user;
    private Notification notification;
    private NotificationDTO dto;

    @BeforeEach
    public void setUp() {
        user = new User();
        notification = Notification.builder()
                .user(user)
                .message("msg")
                .isRead(false)
                .build();
        notification.setNotificationId(1L);
        
        dto = new NotificationDTO();
        dto.setMessage("msg");
        dto.setRead(false);
    }

    @Test
    public void testCreateNotification() {
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);
        
        notificationService.createNotification(user, "Hello");
        
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    public void testGetNotificationsByUser() {
        try (MockedStatic<NotificationRowMapper> mockedMapper = mockStatic(NotificationRowMapper.class)) {
            when(notificationRepository.findByUser(user)).thenReturn(List.of(notification));
            mockedMapper.when(() -> NotificationRowMapper.toDto(notification)).thenReturn(dto);
            
            List<NotificationDTO> result = notificationService.getNotificationsByUser(user);
            assertEquals(1, result.size());
            assertEquals("msg", result.get(0).getMessage());
        }
    }

    @Test
    public void testGetUnreadNotifications() {
        try (MockedStatic<NotificationRowMapper> mockedMapper = mockStatic(NotificationRowMapper.class)) {
            when(notificationRepository.findByUserAndIsReadFalse(user)).thenReturn(List.of(notification));
            mockedMapper.when(() -> NotificationRowMapper.toDto(notification)).thenReturn(dto);
            
            List<NotificationDTO> result = notificationService.getUnreadNotifications(user);
            assertEquals(1, result.size());
            assertFalse(result.get(0).isRead());
        }
    }

    @Test
    public void testGetAllNotifications() {
        try (MockedStatic<NotificationRowMapper> mockedMapper = mockStatic(NotificationRowMapper.class)) {
            when(notificationRepository.findAll()).thenReturn(List.of(notification));
            mockedMapper.when(() -> NotificationRowMapper.toDto(notification)).thenReturn(dto);
            
            List<NotificationDTO> result = notificationService.getAllNotifications();
            assertEquals(1, result.size());
            assertEquals("msg", result.get(0).getMessage());
        }
    }

    @Test
    public void testMarkAsRead_found() {
        Notification notif = mock(Notification.class);
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notif));
        
        notificationService.markAsRead(1L);
        
        verify(notif).setRead(true);
        verify(notificationRepository).save(notif);
    }

    @Test
    public void testMarkAsRead_notFound() {
        when(notificationRepository.findById(2L)).thenReturn(Optional.empty());
        
        notificationService.markAsRead(2L);
        
        verify(notificationRepository, never()).save(any());
    }
}