// Java
package com.cityStar.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import com.cityStar.dto.*;
import com.cityStar.enums.Status;
import com.cityStar.model.*;
import com.cityStar.repository.*;
import com.cityStar.rowmapper.*;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
public class DoctorServiceTest {

    @Mock private IavailabilityRepository availabilityrepo;
    @Mock private NotificationService notificationService;
    @Mock private IdoctorRepository doctorRepository;
    @Mock private StorageService storageService;
    @Mock private IuserRepository userRepository;
    @Mock private IappointmentRepository appointmentRepository;

    @InjectMocks
    private DoctorService doctorService;

    private Doctor doctor;
    private Availability availability;
    private Appointment appointment;
    private DoctorDTO doctorDTO;
    private MultipartFile file;

    @BeforeEach
    public void setUp() {
        doctor = new Doctor();
        doctor.setEmail("doc@example.com");
        doctor.setFirstName("Doc");

        availability = new Availability();
        availability.setAvailabilityId(1L);
        availability.setIsAvailable(false);
        availability.setDoctor(doctor);

        appointment = new Appointment();
        appointment.setAppointmentId(1L);
        appointment.setAvailability(availability);
        appointment.setStatus(Status.Pending);
        appointment.setAppointmentTime(LocalDateTime.now());
        appointment.setPatient(new Patient());

        doctorDTO = new DoctorDTO();
        file = mock(MultipartFile.class);
    }

    @Test
    public void testFindByAvailabilityId_found() {
        when(availabilityrepo.findById(1L)).thenReturn(Optional.of(availability));
        assertEquals(availability, doctorService.findByAvailabilityId(1L));
    }

    @Test
    public void testFindByAvailabilityId_notFound() {
        when(availabilityrepo.findById(1L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> doctorService.findByAvailabilityId(1L));
    }

    @Test
    public void testAddAvailability_setsIsAvailableAndSaves() {
        Availability avail = new Availability();
        avail.setIsAvailable(false);
        doctorService.addAvailability(avail);
        assertTrue(avail.getIsAvailable());
        verify(availabilityrepo).save(avail);
    }

    @Test
    public void testGetAllAvailabilitiesForToday() {
        LocalDate today = LocalDate.now();
        List<Availability> availList = List.of(availability);
        when(availabilityrepo.findByAvailableDateAndIsAvailableTrue(today)).thenReturn(availList);

        AvailabilityDTO dto = new AvailabilityDTO();
        try (MockedStatic<AvailabilityRowMapper> mocked = Mockito.mockStatic(AvailabilityRowMapper.class)) {
            mocked.when(() -> AvailabilityRowMapper.toDtoWithDoctor(availability)).thenReturn(dto);

            List<AvailabilityDTO> result = doctorService.getAllAvailabilitiesForToday();
            assertEquals(1, result.size());
        }
    }

    @Test
    public void testGetLatestAvailabilityForToday_found() {
        when(doctorRepository.findByEmail(anyString())).thenReturn(Optional.of(doctor));
        when(availabilityrepo.findTopByAvailableDateAndDoctorOrderByAvailabilityIdDesc(any(), eq(doctor)))
            .thenReturn(Optional.of(availability));

        assertEquals(availability, doctorService.getLatestAvailabilityForToday("doc@example.com"));
    }

    @Test
    public void testGetLatestAvailabilityForToday_doctorNotFound() {
        when(doctorRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> doctorService.getLatestAvailabilityForToday("doc@example.com"));
    }

    @Test
    public void testUpdateDoctorProfile_updatesFieldsAndSaves() {
        when(doctorRepository.findByEmail(anyString())).thenReturn(Optional.of(doctor));
        when(file.isEmpty()).thenReturn(false);
        when(storageService.saveImg(any(), eq(file))).thenReturn("img/path");

        doctorDTO.setFirstName("NewName");
        doctorDTO.setBio("bio");
        doctorDTO.setEmail("new@email.com");

        doctorService.updateDoctorProfile("doc@example.com", doctorDTO, file);

        assertEquals("NewName", doctor.getFirstName());
        assertEquals("bio", doctor.getBio());
        assertEquals("new@email.com", doctor.getEmail());
        assertEquals("img/path", doctor.getProfilePath());
        verify(userRepository).save(doctor);
    }

    @Test
    public void testGetAppointmentsByDoctorAndStatus() {
        when(appointmentRepository.findByAvailability_DoctorAndStatus(doctor, Status.Pending))
            .thenReturn(List.of(appointment));
        AppointmentDTO dto = new AppointmentDTO();
        try (MockedStatic<AppointmentRowMapper> mocked = Mockito.mockStatic(AppointmentRowMapper.class)) {
            mocked.when(() -> AppointmentRowMapper.toDto(appointment)).thenReturn(dto);

            List<AppointmentDTO> result = doctorService.getAppointmentsByDoctorAndStatus(doctor, Status.Pending);
            assertEquals(1, result.size());
        }
    }

    @Test
    public void testGetAllAppointmentsByDoctor() {
        when(appointmentRepository.findByAvailability_Doctor(doctor)).thenReturn(List.of(appointment));
        AppointmentDTO dto = new AppointmentDTO();
        try (MockedStatic<AppointmentRowMapper> mocked = Mockito.mockStatic(AppointmentRowMapper.class)) {
            mocked.when(() -> AppointmentRowMapper.toDto(appointment)).thenReturn(dto);

            List<AppointmentDTO> result = doctorService.getAllAppointmentsByDoctor(doctor);
            assertEquals(1, result.size());
        }
    }

    @Test
    public void testUpdateStatus_updatesStatusAndNotifies() {
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        doctorService.updateStatus(1L, Status.Confirmed);

        assertEquals(Status.Confirmed, appointment.getStatus());
        verify(notificationService).createNotification(any(), contains("Confirmed"));
    }

    @Test
    public void testUpdateStatus_notFound() {
        when(appointmentRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> doctorService.updateStatus(1L, Status.Confirmed));
    }

    @Test
    public void testGetAppointmentCountsByStatusForDoctor() {
        when(appointmentRepository.countByAvailability_Doctor(doctor)).thenReturn(10L);
        when(appointmentRepository.countByAvailability_DoctorAndStatus(doctor, Status.Pending)).thenReturn(2L);
        when(appointmentRepository.countByAvailability_DoctorAndStatus(doctor, Status.Confirmed)).thenReturn(3L);
        when(appointmentRepository.countByAvailability_DoctorAndStatus(doctor, Status.Cancelled)).thenReturn(1L);

        Map<String, Long> result = doctorService.getAppointmentCountsByStatusForDoctor(doctor);
        assertEquals(10L, result.get("all"));
        assertEquals(2L, result.get("pending"));
        assertEquals(3L, result.get("confirmed"));
        assertEquals(1L, result.get("cancelled"));
    }

    @Test
    public void testGetTopThreePendingTodayForDoctor() {
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.atTime(LocalTime.MAX);

        when(appointmentRepository
            .findTop3ByAvailability_DoctorAndStatusAndAppointmentTimeBetweenOrderByAppointmentTimeAsc(
                eq(doctor), eq(Status.Pending), eq(start), eq(end)))
            .thenReturn(List.of(appointment));

        AppointmentDTO dto = new AppointmentDTO();
        try (MockedStatic<AppointmentRowMapper> mocked = Mockito.mockStatic(AppointmentRowMapper.class)) {
            mocked.when(() -> AppointmentRowMapper.toDto(appointment)).thenReturn(dto);

            List<AppointmentDTO> result = doctorService.getTopThreePendingTodayForDoctor(doctor);
            assertEquals(1, result.size());
        }
    }

    @Test
    public void testGetDoctorAppointmentStats() {
        List<Appointment> all = new ArrayList<>();
        Appointment a1 = new Appointment();
        a1.setAppointmentTime(LocalDateTime.now());
        Availability avail = new Availability();
        avail.setDoctor(doctor);
        a1.setAvailability(avail);
        a1.setPatient(new Patient());
        all.add(a1);

        when(appointmentRepository.findAll()).thenReturn(all);

        Map<String, Long> stats = doctorService.getDoctorAppointmentStats(doctor);
        assertTrue(stats.containsKey("todayPatients"));
        assertTrue(stats.containsKey("monthlyAppointments"));
        assertTrue(stats.containsKey("newAppointments"));
    }

    @Test
    public void testGetTodayAppointmentDTOs() {
        when(appointmentRepository.findByAvailability_DoctorAndAppointmentTimeBetween(any(), any(), any()))
            .thenReturn(List.of(appointment));

        AppointmentDTO dto = new AppointmentDTO();
        try (MockedStatic<AppointmentRowMapper> mocked = Mockito.mockStatic(AppointmentRowMapper.class)) {
            mocked.when(() -> AppointmentRowMapper.toDto(appointment)).thenReturn(dto);

            List<AppointmentDTO> result = doctorService.getTodayAppointmentDTOs(doctor);
            assertEquals(1, result.size());
        }
    }

    @Test
    public void testCountUniquePatientsThisMonth() {
        Patient u1 = new Patient();
        Patient u2 = new Patient();

        Appointment a1 = new Appointment();
        a1.setPatient(u1);
        Appointment a2 = new Appointment();
        a2.setPatient(u2);

        when(appointmentRepository.findByAvailability_DoctorAndAppointmentTimeBetween(any(), any(), any()))
            .thenReturn(List.of(a1, a2));

        long count = doctorService.countUniquePatientsThisMonth(doctor);
        assertEquals(2, count);
    }
}
