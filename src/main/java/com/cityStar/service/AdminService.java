package com.cityStar.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.cityStar.dto.AppointmentDTO;
import com.cityStar.enums.Status;
import com.cityStar.repository.IappointmentRepository;
import com.cityStar.rowmapper.AppointmentRowMapper;

@Service
public class AdminService {
    private final IappointmentRepository appointmentRepository;
    public AdminService(IappointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
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

}
