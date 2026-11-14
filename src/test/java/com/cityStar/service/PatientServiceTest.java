package com.cityStar.service;

import com.cityStar.dto.AppointmentDTO;
import com.cityStar.dto.AvailabilityDTO;
import com.cityStar.dto.PatientDTO;
import com.cityStar.model.*;
import com.cityStar.repository.IappointmentRepository;
import com.cityStar.repository.IavailabilityRepository;
import com.cityStar.repository.IpatientRepository;
import com.cityStar.repository.IuserRepository;
import com.cityStar.rowmapper.AppointmentRowMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PatientServiceTest {

    @Mock
    private IuserRepository userRepository;
    @Mock
    private StorageService storageService;
    @Mock
    private IpatientRepository patientRepository;
    @Mock
    private IappointmentRepository appointmentRepository;
    @Mock
    private IavailabilityRepository availabilityRepository;

    @InjectMocks
    private PatientService patientService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void updatePatientProfile_shouldUpdateFieldsAndSave() {
        Patient patient = new Patient();
        patient.setEmail("old@mail.com");
        PatientDTO dto = new PatientDTO();
        dto.setFirstName("John");
        dto.setMiddleName("M");
        dto.setLastName("Doe");
        dto.setEmail("new@mail.com");
        dto.setAge(30);
        dto.setAddress("Yangon");

        MultipartFile file = mock(MultipartFile.class);

        when(patientRepository.findByEmail("old@mail.com")).thenReturn(Optional.of(patient));
        when(file.isEmpty()).thenReturn(false);
        when(storageService.saveImg(patient, file)).thenReturn("/img/patient.jpg");

        patientService.updatePatientProfile("old@mail.com", dto, file);

        assertEquals("John", patient.getFirstName());
        assertEquals("M", patient.getMiddleName());
        assertEquals("Doe", patient.getLastName());
        assertEquals("new@mail.com", patient.getEmail());
        assertEquals(30, patient.getAge());
        assertEquals("Yangon", patient.getAddress());
        assertEquals("/img/patient.jpg", patient.getProfilePath());
        verify(userRepository).save(patient);
    }

    @Test
    void updatePatientProfile_shouldThrowIfPatientNotFound() {
        when(patientRepository.findByEmail("notfound@mail.com")).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () ->
                patientService.updatePatientProfile("notfound@mail.com", new PatientDTO(), null));
    }

    @Test
    void createAppointment_shouldSaveAppointmentAndUpdateAvailability() {
        Patient patient = new Patient();
        Availability availability = new Availability();
        availability.setAvailabilityId(1L);
        AppointmentDTO dto = new AppointmentDTO();
        dto.setAppointmentInfo("info");
        dto.setAppointmentTime(LocalDateTime.now());
        AvailabilityDTO availDTO = new AvailabilityDTO();
        availDTO.setAvailabilityId(1L);
        dto.setAvailability(availDTO);

        when(availabilityRepository.findById(1L)).thenReturn(Optional.of(availability));

        patientService.createAppointment(dto, patient);

        verify(appointmentRepository).save(any(Appointment.class));
        assertFalse(availability.getIsAvailable());
        verify(availabilityRepository).save(availability);
    }

    @Test
    void createAppointment_shouldThrowIfAvailabilityNotFound() {
        AppointmentDTO dto = new AppointmentDTO();
        AvailabilityDTO availDTO = new AvailabilityDTO();
        availDTO.setAvailabilityId(2L);
        dto.setAvailability(availDTO);

        when(availabilityRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                patientService.createAppointment(dto, new Patient()));
    }

    @Test
    void getAppointmentDTOsByPatient_shouldReturnMappedDTOs() {
        Patient patient = new Patient();
        Appointment a1 = new Appointment();
        Appointment a2 = new Appointment();
        List<Appointment> appointments = Arrays.asList(a1, a2);
        AppointmentDTO dto1 = new AppointmentDTO();
        AppointmentDTO dto2 = new AppointmentDTO();

        when(appointmentRepository.findByPatient(patient)).thenReturn(appointments);

        try (MockedStatic<AppointmentRowMapper> mocked = mockStatic(AppointmentRowMapper.class)) {
            mocked.when(() -> AppointmentRowMapper.toDto(a1)).thenReturn(dto1);
            mocked.when(() -> AppointmentRowMapper.toDto(a2)).thenReturn(dto2);

            List<AppointmentDTO> result = patientService.getAppointmentDTOsByPatient(patient);
            assertEquals(2, result.size());
            assertTrue(result.contains(dto1));
            assertTrue(result.contains(dto2));
        }
    }

    @Test
    void getTop3AppointmentDTOsByPatient_shouldReturnTop3MappedDTOs() {
        Patient patient = new Patient();
        Appointment a1 = new Appointment();
        Appointment a2 = new Appointment();
        Appointment a3 = new Appointment();
        Appointment a4 = new Appointment();
        List<Appointment> appointments = Arrays.asList(a1, a2, a3, a4);
        AppointmentDTO dto1 = new AppointmentDTO();
        AppointmentDTO dto2 = new AppointmentDTO();
        AppointmentDTO dto3 = new AppointmentDTO();

        when(appointmentRepository.findByPatient(patient)).thenReturn(appointments);

        try (MockedStatic<AppointmentRowMapper> mocked = mockStatic(AppointmentRowMapper.class)) {
            mocked.when(() -> AppointmentRowMapper.toDto(a1)).thenReturn(dto1);
            mocked.when(() -> AppointmentRowMapper.toDto(a2)).thenReturn(dto2);
            mocked.when(() -> AppointmentRowMapper.toDto(a3)).thenReturn(dto3);

            List<AppointmentDTO> result = patientService.getTop3AppointmentDTOsByPatient(patient);
            assertEquals(3, result.size());
            assertTrue(result.contains(dto1));
            assertTrue(result.contains(dto2));
            assertTrue(result.contains(dto3));
        }
    }

    @Test
    void searchTodayAvailabilities_shouldFilterByDoctorNameAndTime() {
        LocalDate today = LocalDate.now();
        Doctor doctor1 = new Doctor();
        doctor1.setFirstName("Alice");
        doctor1.setLastName("Smith");
        Doctor doctor2 = new Doctor();
        doctor2.setFirstName("Bob");
        doctor2.setLastName("Jones");

        Availability a1 = new Availability();
        a1.setDoctor(doctor1);
        a1.setStartTime(LocalTime.of(9, 0));
        a1.setEndTime(LocalTime.of(10, 0));
        Availability a2 = new Availability();
        a2.setDoctor(doctor2);
        a2.setStartTime(LocalTime.of(11, 0));
        a2.setEndTime(LocalTime.of(12, 0));
        List<Availability> availabilities = Arrays.asList(a1, a2);

        when(availabilityRepository.findByAvailableDate(today)).thenReturn(availabilities);

        List<Availability> result = patientService.searchTodayAvailabilities("Alice", LocalTime.of(8, 0), LocalTime.of(10, 0));
        assertEquals(1, result.size());
        assertEquals(a1, result.get(0));
    }
}
