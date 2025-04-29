package com.Clinic.Appointment.System.task.repository;

import com.Clinic.Appointment.System.task.model.patient_mod;
import org.springframework.data.jpa.repository.JpaRepository;

public interface patient_repo extends JpaRepository<patient_mod, String> {}