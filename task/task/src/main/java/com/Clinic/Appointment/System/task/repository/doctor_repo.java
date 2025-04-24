package com.Clinic.Appointment.System.task.repository;

import com.Clinic.Appointment.System.task.model.doctor_mod;
import org.springframework.data.jpa.repository.JpaRepository;

public interface doctor_repo extends JpaRepository<doctor_mod, Long> {
}
