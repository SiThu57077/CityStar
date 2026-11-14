package com.cityStar.service;

import com.cityStar.dto.UserDTO;
import com.cityStar.exception.EmailDuplicationException;
import com.cityStar.model.User;
import com.cityStar.repository.IuserRepository;
import com.cityStar.rowmapper.UserRowMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private IuserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void saveUser_shouldSaveUserWithEncodedPasswordAndDefaultProfilePath() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("plain");
        user.setProfilePath(null);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("plain")).thenReturn("encoded");

        userService.saveUser(user);

        assertEquals("encoded", user.getPassword());
        assertEquals("/img/default_profile_photo.jpg", user.getProfilePath());
        verify(userRepository).save(user);
    }

    @Test
    void saveUser_shouldSaveUserWithCustomProfilePath() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("plain");
        user.setProfilePath("/img/custom.jpg");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("plain")).thenReturn("encoded");

        userService.saveUser(user);

        assertEquals("encoded", user.getPassword());
        assertEquals("/img/custom.jpg", user.getProfilePath());
        verify(userRepository).save(user);
    }

    @Test
    void saveUser_shouldThrowExceptionIfEmailExists() {
        User user = new User();
        user.setEmail("test@example.com");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        assertThrows(EmailDuplicationException.class, () -> userService.saveUser(user));
        verify(userRepository, never()).save(any());
    }

    @Test
    void findByEmail_shouldReturnUser() {
        User user = new User();
        user.setEmail("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        User result = userService.findByEmail("test@example.com");
        assertEquals(user, result);
    }

    @Test
    void findAllDto_shouldReturnUserDTOList() {
        User user1 = new User();
        User user2 = new User();
        List<User> users = Arrays.asList(user1, user2);
        UserDTO dto1 = new UserDTO();
        UserDTO dto2 = new UserDTO();

        when(userRepository.findAll()).thenReturn(users);

        try (MockedStatic<UserRowMapper> mocked = mockStatic(UserRowMapper.class)) {
            mocked.when(() -> UserRowMapper.toDtoWithoutPassword(user1)).thenReturn(dto1);
            mocked.when(() -> UserRowMapper.toDtoWithoutPassword(user2)).thenReturn(dto2);

            List<UserDTO> result = userService.findAllDto();
            assertEquals(2, result.size());
            assertTrue(result.contains(dto1));
            assertTrue(result.contains(dto2));
        }
    }

}
