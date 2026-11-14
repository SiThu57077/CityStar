package com.cityStar.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cityStar.enums.Status;
import com.cityStar.model.Appointment;
import com.cityStar.model.Doctor;
import com.cityStar.model.Patient;

@Repository
public interface IappointmentRepository extends JpaRepository<Appointment, Long>{
    List<Appointment> findByPatient(Patient patient);
    List<Appointment> findByAvailability_DoctorAndStatus(Doctor doctor, Status status);
    List<Appointment> findByAvailability_Doctor(Doctor doctor);
    List<Appointment> findTop3ByAvailability_DoctorAndStatusAndAppointmentTimeBetweenOrderByAppointmentTimeAsc(
    Doctor doctor, Status status, LocalDateTime start, LocalDateTime end
    );

    List<Appointment> findByAvailability_DoctorAndAppointmentTimeBetween(
                Doctor doctor,
                LocalDateTime startDate,
                LocalDateTime endDate
    );

    List<Appointment> findByStatus(Status status);

    long countByAvailability_Doctor(Doctor doctor);

    long countByAvailability_DoctorAndStatus(Doctor doctor, Status status);

    List<Appointment> findByStatusAndAppointmentTimeBetween(Status status, LocalDateTime start, LocalDateTime end);

    List<Appointment> findByAppointmentTimeBetween(LocalDateTime start, LocalDateTime end);

    List<Appointment> findTop5ByOrderByAppointmentTimeAsc();


}