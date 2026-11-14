package com.cityStar.service;

import com.cityStar.dto.AppointmentDTO;
import com.cityStar.dto.DoctorDTO;
import com.cityStar.enums.Status;
import com.cityStar.model.Appointment;
import com.cityStar.model.Doctor;
import com.cityStar.repository.IappointmentRepository;
import com.cityStar.repository.IdoctorRepository;
import com.cityStar.rowmapper.AppointmentRowMapper;
import com.cityStar.rowmapper.DoctorRowMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private IappointmentRepository appointmentRepository;
    @Mock
    private IdoctorRepository doctorRepository;

    @InjectMocks
    private AdminService adminService;

    private Appointment dummyAppointment;
    private Doctor dummyDoctor;

    @BeforeEach
    void setUp() {
        dummyAppointment = new Appointment();
        dummyAppointment.setAppointmentTime(LocalDateTime.now());
        dummyAppointment.setStatus(Status.Pending);

        dummyDoctor = new Doctor();
        dummyDoctor.setFirstName("John");
    }

    @Test
    void getAppointmentsByStatus_shouldReturnMappedDTOs() {
        when(appointmentRepository.findByStatus(Status.Pending)).thenReturn(List.of(dummyAppointment));
        try (MockedStatic<AppointmentRowMapper> mocked = mockStatic(AppointmentRowMapper.class)) {
            mocked.when(() -> AppointmentRowMapper.toDto(dummyAppointment)).thenReturn(new AppointmentDTO());
            List<AppointmentDTO> result = adminService.getAppointmentsByStatus(Status.Pending);
            assertEquals(1, result.size());
        }
    }

    @Test
    void getAllAppointments_shouldReturnMappedDTOs() {
        when(appointmentRepository.findAll()).thenReturn(List.of(dummyAppointment));
        try (MockedStatic<AppointmentRowMapper> mocked = mockStatic(AppointmentRowMapper.class)) {
            mocked.when(() -> AppointmentRowMapper.toDto(dummyAppointment)).thenReturn(new AppointmentDTO());
            List<AppointmentDTO> result = adminService.getAllAppointments();
            assertEquals(1, result.size());
        }
    }

    @Test
    void getAppointmentsByStatusForToday_shouldReturnMappedDTOs() {
        when(appointmentRepository.findByStatusAndAppointmentTimeBetween(any(), any(), any()))
                .thenReturn(List.of(dummyAppointment));
        try (MockedStatic<AppointmentRowMapper> mocked = mockStatic(AppointmentRowMapper.class)) {
            mocked.when(() -> AppointmentRowMapper.toDto(dummyAppointment)).thenReturn(new AppointmentDTO());
            List<AppointmentDTO> result = adminService.getAppointmentsByStatusForToday(Status.Pending);
            assertEquals(1, result.size());
        }
    }

    @Test
    void getAllAppointmentsForToday_shouldReturnMappedDTOs() {
        when(appointmentRepository.findByAppointmentTimeBetween(any(), any()))
                .thenReturn(List.of(dummyAppointment));
        try (MockedStatic<AppointmentRowMapper> mocked = mockStatic(AppointmentRowMapper.class)) {
            mocked.when(() -> AppointmentRowMapper.toDto(dummyAppointment)).thenReturn(new AppointmentDTO());
            List<AppointmentDTO> result = adminService.getAllAppointmentsForToday();
            assertEquals(1, result.size());
        }
    }

    @Test
    void getAppointmentsByDate_shouldReturnMappedDTOs() {
        LocalDate testDate = LocalDate.of(2025, 1, 1);
        when(appointmentRepository.findByAppointmentTimeBetween(any(), any()))
                .thenReturn(List.of(dummyAppointment));
        try (MockedStatic<AppointmentRowMapper> mocked = mockStatic(AppointmentRowMapper.class)) {
            mocked.when(() -> AppointmentRowMapper.toDto(dummyAppointment)).thenReturn(new AppointmentDTO());
            List<AppointmentDTO> result = adminService.getAppointmentsByDate(testDate);
            assertEquals(1, result.size());
        }
    }

    @Test
    void getAppointmentsByStatusForDate_shouldReturnMappedDTOs() {
        LocalDate testDate = LocalDate.of(2025, 1, 1);
        when(appointmentRepository.findByStatusAndAppointmentTimeBetween(any(), any(), any()))
                .thenReturn(List.of(dummyAppointment));
        try (MockedStatic<AppointmentRowMapper> mocked = mockStatic(AppointmentRowMapper.class)) {
            mocked.when(() -> AppointmentRowMapper.toDto(dummyAppointment)).thenReturn(new AppointmentDTO());
            List<AppointmentDTO> result = adminService.getAppointmentsByStatusForDate(Status.Pending, testDate);
            assertEquals(1, result.size());
        }
    }

    @Test
    void getTop5UpcomingAppointments_shouldReturnMappedDTOs() {
        when(appointmentRepository.findTop5ByOrderByAppointmentTimeAsc()).thenReturn(List.of(dummyAppointment));
        try (MockedStatic<AppointmentRowMapper> mocked = mockStatic(AppointmentRowMapper.class)) {
            mocked.when(() -> AppointmentRowMapper.toDto(dummyAppointment)).thenReturn(new AppointmentDTO());
            List<AppointmentDTO> result = adminService.getTop5UpcomingAppointments();
            assertEquals(1, result.size());
        }
    }

    @Test
    void getTop5Doctors_shouldReturnMappedDTOs() {
        when(doctorRepository.findTop5ByOrderByFirstNameAsc()).thenReturn(List.of(dummyDoctor));
        try (MockedStatic<DoctorRowMapper> mocked = mockStatic(DoctorRowMapper.class)) {
            mocked.when(() -> DoctorRowMapper.toDtoWithoutPassword(dummyDoctor)).thenReturn(new DoctorDTO());
            List<DoctorDTO> result = adminService.getTop5Doctors();
            assertEquals(1, result.size());
        }
    }

    @Test
    void getMonthlyAppointmentCounts_shouldReturnCorrectMap() {
        YearMonth current = YearMonth.now();
        LocalDateTime dateInCurrentMonth = current.atDay(5).atTime(10, 0);
        dummyAppointment.setAppointmentTime(dateInCurrentMonth);
        dummyAppointment.setStatus(Status.Pending);

        when(appointmentRepository.findByAppointmentTimeBetween(any(), any()))
                .thenReturn(List.of(dummyAppointment));

        Map<String, List<Long>> result = adminService.getMonthlyAppointmentCounts();

        assertEquals(12, result.get("Pending").size());
        assertTrue(result.get("Pending").stream().anyMatch(count -> count > 0));
        assertTrue(result.containsKey("Pending"));
    }
}

