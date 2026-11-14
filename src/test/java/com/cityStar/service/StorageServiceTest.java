package com.cityStar.service;

import com.cityStar.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StorageServiceTest {

    @Test
    void saveImg_shouldReturnExpectedPath() {
        // Arrange
        StorageService storageService = mock(StorageService.class);
        User user = new User();
        MultipartFile file = mock(MultipartFile.class);

        when(storageService.saveImg(user, file)).thenReturn("/img/user123.jpg");

        // Act
        String result = storageService.saveImg(user, file);

        // Assert
        assertEquals("/img/user123.jpg", result);
        verify(storageService).saveImg(user, file);
    }
}

