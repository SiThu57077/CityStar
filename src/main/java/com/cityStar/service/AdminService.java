package com.cityStar.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.cityStar.dto.AppointmentDTO;
import com.cityStar.dto.DoctorDTO;
import com.cityStar.enums.Status;
import com.cityStar.model.Appointment;
import com.cityStar.repository.IappointmentRepository;
import com.cityStar.repository.IdoctorRepository;
import com.cityStar.rowmapper.AppointmentRowMapper;
import com.cityStar.rowmapper.DoctorRowMapper;

@Service
public class AdminService {
    private final IappointmentRepository appointmentRepository;
    private final IdoctorRepository doctorRepository;
    public AdminService(IappointmentRepository appointmentRepository,
                        IdoctorRepository doctorRepository) {
        this.appointmentRepository = appointmentRepository;
        this.doctorRepository = doctorRepository;
    }

    public List<AppointmentDTO> getAppointmentsByStatus(Status status) {
        return appointmentRepository.findByStatus(status)
                .stream().map(AppointmentRowMapper::toDto).collect(Collectors.toList());
    }

    public List<AppointmentDTO> getAllAppointments() {
        return appointmentRepository.findAll()
                .stream().map(AppointmentRowMapper::toDto).collect(Collectors.toList());
    }

    public List<AppointmentDTO> getAppointmentsByStatusForToday(Status status) {
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        LocalDateTime endOfToday = startOfToday.plusDays(1);

        return appointmentRepository.findByStatusAndAppointmentTimeBetween(status, startOfToday, endOfToday)
                .stream()
                .map(AppointmentRowMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<AppointmentDTO> getAllAppointmentsForToday() {
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        LocalDateTime endOfToday = startOfToday.plusDays(1);

        return appointmentRepository.findByAppointmentTimeBetween(startOfToday, endOfToday)
                .stream()
                .map(AppointmentRowMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<AppointmentDTO> getAppointmentsByDate(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = start.plusDays(1);
    
        return appointmentRepository.findByAppointmentTimeBetween(start, end)
                .stream()
                .map(AppointmentRowMapper::toDto)
                .collect(Collectors.toList());
    }
    
    public List<AppointmentDTO> getAppointmentsByStatusForDate(Status status, LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = start.plusDays(1);
    
        return appointmentRepository.findByStatusAndAppointmentTimeBetween(status, start, end)
                .stream()
                .map(AppointmentRowMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<AppointmentDTO> getTop5UpcomingAppointments() {
        return appointmentRepository.findTop5ByOrderByAppointmentTimeAsc()
                .stream()
                .map(AppointmentRowMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<DoctorDTO> getTop5Doctors() {
        return doctorRepository.findTop5ByOrderByFirstNameAsc()
                .stream()
                .limit(5)
                .map(DoctorRowMapper::toDtoWithoutPassword)
                .collect(Collectors.toList());
    }

    public Map<String, List<Long>> getMonthlyAppointmentCounts() {
        YearMonth current = YearMonth.now();
        List<Appointment> appointments = appointmentRepository.findByAppointmentTimeBetween(
            current.atDay(1).atStartOfDay(),
            current.atEndOfMonth().plusDays(1).atStartOfDay()
        );

        Map<Status, List<Long>> statusMap = new HashMap<>();
        for (Status status : Status.values()) {
            statusMap.put(status, new ArrayList<>(Collections.nCopies(12, 0L)));
        }

        for (Appointment a : appointments) {
            int month = a.getAppointmentTime().getMonthValue() - 1;
            statusMap.computeIfPresent(a.getStatus(), (k, v) -> {
                v.set(month, v.get(month) + 1);
                return v;
            });
        }

        Map<String, List<Long>> result = new HashMap<>();
        statusMap.forEach((k, v) -> result.put(k.name(), v));
        return result;
    }

}
