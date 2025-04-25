package com.Clinic.Appointment.System.task.service;

import com.Clinic.Appointment.System.task.model.appointment_mod;

import java.util.*;

public interface clinic_serv {

        //book,reschedule,cancel
        String bookAppointment(String doctorType, String patientName, int time);
        String rescheduleAppointment(String doctorType, String patientName, int time);
        String cancelAppointment(String patientName);

        //getting the timings the doc is available for patients
        List<Integer> getAvailableSlots(String doctorType);

        //getting the schedule of the doc
        List<appointment_mod> getDoctorSchedule(String doctorType);
}
