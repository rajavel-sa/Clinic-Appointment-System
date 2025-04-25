package com.Clinic.Appointment.System.task.service;

import com.Clinic.Appointment.System.task.model.appointment_mod;
import com.Clinic.Appointment.System.task.model.doctor_mod;
import com.Clinic.Appointment.System.task.model.patient_mod;
import com.Clinic.Appointment.System.task.repository.appointment_repo;
import com.Clinic.Appointment.System.task.repository.doctor_repo;
import com.Clinic.Appointment.System.task.repository.patient_repo;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

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

    List<Integer> validSlots = Arrays.asList(5, 6, 7, 8, 9);

    //book appointments + Prevent double-booking
    @Override
    public String bookAppointment(String doctorType, String patientName, int time) {

        Optional<doctor_mod> doctorOpt = doctorRepo.findAll()
                .stream()
                .filter(doc -> doc.getD_spl().equalsIgnoreCase(doctorType))
                .findFirst();

        if (doctorOpt.isEmpty()) {
            return "x No doctor found with specialization: " + doctorType;
        }

        doctor_mod doctor = doctorOpt.get();

        List<Integer> validSlots = Arrays.asList(5, 6, 7, 8, 9);
        if (!validSlots.contains(time)) {
            return "x Invalid time slot: " + time + ". Allowed slots are: " + validSlots;
        }


        Optional<appointment_mod> existingAppointmentOpt = appointmentRepo.findAll()
                .stream()
                .filter(app -> app.getP_name().equalsIgnoreCase(patientName))
                .findFirst();

        if (existingAppointmentOpt.isPresent()) {
            appointment_mod existingAppointment = existingAppointmentOpt.get();

            if (existingAppointment.getD_spl().equalsIgnoreCase(doctorType)) {
                return "x Patient " + patientName + " already has an appointment with Dr. "
                        + existingAppointment.getD_name() + " at time slot " + existingAppointment.getTime() + ".";
            } else {
                return "x Patient " + patientName + " cannot book with Dr. " + doctor.getD_name() +
                        " (" + doctorType + ") because they already have an appointment with Dr. " +
                        existingAppointment.getD_name() + " (" + existingAppointment.getD_spl() + ") at " +
                        "time slot " + existingAppointment.getTime() + ".";
            }
        }


        boolean isAlreadyBooked = appointmentRepo.findAll()
                .stream()
                .anyMatch(app -> app.getD_spl().equalsIgnoreCase(doctorType)
                        && app.getTime() == time);

        if (isAlreadyBooked) {
            List<Integer> bookedTimes = appointmentRepo.findAll()
                    .stream()
                    .filter(app -> app.getD_spl().equalsIgnoreCase(doctorType))
                    .map(appointment_mod::getTime)
                    .collect(Collectors.toList());

            List<Integer> availableSlots = new ArrayList<>(validSlots);
            availableSlots.removeAll(bookedTimes);

            return "! Time slot " + time + " already booked for Dr. " + doctor.getD_name()
                    + ". Try one of these: " + availableSlots;
        }

        appointment_mod appointment = new appointment_mod();
        appointment.setD_name(doctor.getD_name());
        appointment.setD_spl(doctor.getD_spl());
        appointment.setP_name(patientName);
        appointment.setTime(time);

        appointmentRepo.save(appointment);

        return "Done. Appointment booked with Dr. " + doctor.getD_name() + " at time slot " + time;
    }

    //reschedule appointments
    @Override
    public String rescheduleAppointment(String doctorType, String patientName, int time) {

        Optional<appointment_mod> existing = appointmentRepo.findAll()
                .stream()
                .filter(app -> app.getP_name().equalsIgnoreCase(patientName)
                        && app.getD_spl().equalsIgnoreCase(doctorType))
                .findFirst();

        if (existing.isEmpty()) {
            return " x No appointment found to reschedule for patient: " + patientName;
        }

        boolean isTimeTaken = appointmentRepo.findAll()
                .stream()
                .anyMatch(app -> app.getD_spl().equalsIgnoreCase(doctorType)
                        && app.getTime() == time);

        if (isTimeTaken) {
            return "! Time slot " + time + " is already booked. Pick another.";
        }

        appointment_mod appointment = existing.get();
        appointment.setTime(time);
        appointmentRepo.save(appointment);

        return "Hmmm. Appointment rescheduled to time slot " + time + " for patient: " + patientName;
    }

    //cancel appointment
    @Override
    public String cancelAppointment(String patientName) {
        List<appointment_mod> appointments = appointmentRepo.findAll()
                .stream()
                .filter(app -> app.getP_name().equalsIgnoreCase(patientName))
                .toList();

        if (appointments.isEmpty()) {
            return " x No appointment found for patient: " + patientName;
        }

        appointments.forEach(appointmentRepo::delete);

        return " x Appointment(s) cancelled for patient: " + patientName;
    }

    //for available time slots
    @Override
    public List<Integer> getAvailableSlots(String doctorType) {

        List<appointment_mod> bookedAppointments = appointmentRepo.findAll();

        List<Integer> bookedTimes = bookedAppointments.stream()
                .filter(appointment -> appointment.getD_spl().equalsIgnoreCase(doctorType))
                .map(appointment_mod::getTime)
                .toList();

        List<Integer> availableSlots = new ArrayList<>(validSlots);
        availableSlots.removeAll(bookedTimes);

        return availableSlots;
    }

    //Doctors see their schedule
    @Override
    public List<appointment_mod> getDoctorSchedule(String doctorType) {
        return appointmentRepo.findAll()
                .stream()
                .filter(app -> app.getD_spl().equalsIgnoreCase(doctorType))
                .collect(Collectors.toList());
    }
}