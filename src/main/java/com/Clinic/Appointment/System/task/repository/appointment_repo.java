package com.Clinic.Appointment.System.task.repository;

import com.Clinic.Appointment.System.task.model.appointment_mod;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface appointment_repo extends JpaRepository<appointment_mod, Integer> {}