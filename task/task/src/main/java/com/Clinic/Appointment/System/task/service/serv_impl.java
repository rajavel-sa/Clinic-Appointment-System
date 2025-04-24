package com.Clinic.Appointment.System.task.service;

import com.Clinic.Appointment.System.task.model.appointment_mod;
import com.Clinic.Appointment.System.task.repository.appointment_repo;
import com.Clinic.Appointment.System.task.repository.doctor_repo;
import com.Clinic.Appointment.System.task.repository.patient_repo;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class serv_impl implements clinic_serv{

    private final doctor_repo doctorRepo;
    private final patient_repo patientRepo;
    private final appointment_repo appointmentRepo;

    public serv_impl(doctor_repo doctorRepo, patient_repo patientRepo, appointment_repo appointmentRepo) {
        this.doctorRepo = doctorRepo;
        this.patientRepo = patientRepo;
        this.appointmentRepo = appointmentRepo;
    }

//need to do it for all the 3 doctor, this is temp
    private final List<Integer> allSlots = Arrays.asList(5, 6, 7, 8, 9);

    @Override
    public List<Integer> getAvailableSlots(String doctorType) {
        // Return the available slots based on the doctor type
        return allSlots;
    }

//    private final Map<String, List<Integer>> doctorSchedules = new HashMap<>() {{
//        put("ENT", Arrays.asList(1, 2, 3));
//        put("Dentist", Arrays.asList(4, 5, 6));
//        put("Psychiatrist", Arrays.asList(7, 8, 9));
//    }};

//    @Override
//    public List<Integer> getAvailableSlots(String doctorType) {
//        // Return the available slots based on the doctor type
//        return doctorSchedules.getOrDefault(doctorType, new ArrayList<>());
//    }

    @Override
    public List<appointment_mod> getDoctorSchedule(String doctorType) {
        // Fetch appointments based on doctor's name (doctorType)
        return List.of();
    }

    @Override
    public String bookAppointment(String doctorType, String patientName, int time) {
        return "";
    }

    @Override
    public String rescheduleAppointment(String doctorType, String patientName, int time) {
        return "";
    }

    @Override
    public String cancelAppointment(String patientName) {
        return "";
    }
}
