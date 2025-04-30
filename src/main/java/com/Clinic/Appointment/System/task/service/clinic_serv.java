package com.Clinic.Appointment.System.task.service;

import com.Clinic.Appointment.System.task.model.appointment_mod;

import java.util.*;


public interface clinic_serv {

        // 🔧 Action endpoints
        String bookAppointment(String doctorName, String doctorType, String patientName, int time);
        String rescheduleAppointment(String doctorType, String patientName, int newTime);
        String cancelAppointment(String patientName, int time);

        // 📥 Fetch endpoints
        String getAllPatientSchedules(); // 1. all patients
        String getAppointmentsForPatient(String patientName); // 2. specific patient
        String getAllDoctorsFullSchedule(); // 3. all doctors - free + scheduled
        List<Integer> getAvailableSlots(String doctorType); // 4. free slots of a specialization
        List<appointment_mod> getDoctorSchedule(String doctorType); // 5. full schedule of a doctor type
}

