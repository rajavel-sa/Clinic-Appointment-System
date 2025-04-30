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
import java.util.stream.IntStream;

@Service
public class serv_impl implements clinic_serv {

    private final doctor_repo doctorRepo;
    private final patient_repo patientRepo;
    private final appointment_repo appointmentRepo;

    public serv_impl(doctor_repo doctorRepo, patient_repo patientRepo, appointment_repo appointmentRepo) {
        this.doctorRepo = doctorRepo;
        this.patientRepo = patientRepo;
        this.appointmentRepo = appointmentRepo;
    }

    List<Integer> validSlots = IntStream.range(0, 24)
            .boxed()
            .collect(Collectors.toList());

//List<Integer> validSlots = Arrays.asList(5, 6, 7, 8, 9);

    private boolean isValidTimeSlot(int time) {
        return validSlots.contains(time);
    }

    private List<Integer> getAvailableSlotsForDoctor(String doctorType) {
        List<Integer> bookedTimes = appointmentRepo.findAll()
                .stream()
                .filter(app -> app.getD_spl().equalsIgnoreCase(doctorType))
                .map(appointment_mod::getTime)
                .collect(Collectors.toList());
        List<Integer> availableSlots = new ArrayList<>(validSlots);
        availableSlots.removeAll(bookedTimes);
        return availableSlots;
    }
    private boolean isTimeAlreadyBooked(String doctorType, int time) {
        return appointmentRepo.findAll()
                .stream()
                .anyMatch(app -> app.getD_spl().equalsIgnoreCase(doctorType) && app.getTime() == time);
    }

    private boolean patientExists(String patientName) {
        return patientRepo.findAll()
                .stream()
                .anyMatch(p -> p.getP_name().equalsIgnoreCase(patientName));
    }

    private boolean doctorExists(String doctorType) {
        return getDoctorByType(doctorType).isPresent();
    }

    private List<appointment_mod> fetchAppointmentsByPatient(String patientName) {
        return appointmentRepo.findAll()
                .stream()
                .filter(app -> app.getP_name().equalsIgnoreCase(patientName))
                .collect(Collectors.toList());
    }

    private String formatAppointments(List<appointment_mod> appointments) {
        if (appointments.isEmpty()) {
            return "No appointments.";
        }
        StringBuilder result = new StringBuilder();
        for (appointment_mod app : appointments) {
            result.append(" # ")
                    .append(app.getD_spl()).append(" Dr. ").append(app.getD_name())
                    .append(" at time ").append(app.getTime())
                    .append(".\n");
        }
        return result.toString();
    }

    private Optional<doctor_mod> getDoctorByType(String doctorType) {
        return doctorRepo.findAll()
                .stream()
                .filter(doc -> doc.getD_spl().equalsIgnoreCase(doctorType))
                .findFirst();
    }


    @Override
    public String bookAppointment(String doctorName, String doctorType, String patientName, int time) {
        if (!isValidTimeSlot(time)) {
            return "X Invalid time slot: " + time + ". Valid range: 0-23";
        }

        Optional<doctor_mod> doctorOpt = doctorRepo.findAll().stream()
                .filter(doc -> doc.getD_name().equalsIgnoreCase(doctorName)
                        && doc.getD_spl().equalsIgnoreCase(doctorType))
                .findFirst();

        if (doctorOpt.isEmpty()) {
            return "X Doctor not found with name: " + doctorName + " and specialization: " + doctorType;
        }

        // Check existing patient appointments
        List<appointment_mod> patientAppointments = fetchAppointmentsByPatient(patientName);
        for (appointment_mod app : patientAppointments) {
            if (app.getD_spl().equalsIgnoreCase(doctorType)) {
                return "X Patient already has an appointment with this doctor type.";
            }
            if (app.getTime() == time) {
                return "X Patient already has an appointment at this time with another doctor.";
            }
        }

        long appointmentsForDoctor = appointmentRepo.findAll().stream()
                .filter(app -> app.getD_name().equalsIgnoreCase(doctorName))
                .count();

        if (appointmentsForDoctor >= 3) {
            return "X Doctor has reached max appointments (3).";
        }

        if (isTimeAlreadyBooked(doctorType, time)) {
            return "X Time already booked for this specialization.";
        }

        if (!patientExists(patientName)) {
            patient_mod newP = new patient_mod();
            newP.setP_name(patientName);
            newP.setP_need(doctorType);
            patientRepo.save(newP);
        }

        appointment_mod newApp = new appointment_mod();
        newApp.setD_name(doctorName);
        newApp.setD_spl(doctorType);
        newApp.setP_name(patientName);
        newApp.setTime(time);
        appointmentRepo.save(newApp);

        return "✅ Booked appointment for " + patientName + " with Dr. " + doctorName + " (" + doctorType + ") at " + time + ".";
    }

    @Override
    public String rescheduleAppointment(String doctorType, String patientName, int newTime) {
        Optional<appointment_mod> existingOpt = appointmentRepo.findAll().stream()
                .filter(app -> app.getP_name().equalsIgnoreCase(patientName) &&
                        app.getD_spl().equalsIgnoreCase(doctorType))
                .findFirst();

        if (existingOpt.isEmpty()) {
            return "X No appointment found for " + patientName + " with " + doctorType;
        }

        if (!isValidTimeSlot(newTime)) {
            return "X Invalid new time: " + newTime;
        }

        if (isTimeAlreadyBooked(doctorType, newTime)) {
            return "X Time " + newTime + " already taken.";
        }

        appointment_mod app = existingOpt.get();
        int oldTime = app.getTime();
        app.setTime(newTime);
        appointmentRepo.save(app);

        return "✅ Rescheduled " + patientName + "'s appointment from " + oldTime + " to " + newTime + " with " + doctorType;
    }

    @Override
    public String cancelAppointment(String patientName, int time) {
        List<appointment_mod> match = appointmentRepo.findAll().stream()
                .filter(app -> app.getP_name().equalsIgnoreCase(patientName) && app.getTime() == time)
                .toList();

        if (match.isEmpty()) {
            return "X No appointment found for " + patientName + " at time " + time;
        }

        appointmentRepo.delete(match.get(0));
        return "✅ Cancelled appointment for " + patientName + " at " + time;
    }

    // 1. All patient schedules
    @Override
    public String getAllPatientSchedules() {
        Map<String, List<appointment_mod>> grouped = appointmentRepo.findAll().stream()
                .collect(Collectors.groupingBy(appointment_mod::getP_name));

        if (grouped.isEmpty()) return "X No patient appointments.";

        StringBuilder sb = new StringBuilder();
        for (String name : grouped.keySet()) {
            sb.append("🧑‍⚕️ Patient: ").append(name).append("\n")
                    .append(formatAppointments(grouped.get(name))).append("\n");
        }
        return sb.toString();
    }

    // 2. Specific patient (already implemented)
    @Override
    public String getAppointmentsForPatient(String patientName) {
        if (!patientExists(patientName)) return "X Patient not found.";
        List<appointment_mod> apps = fetchAppointmentsByPatient(patientName);
        return apps.isEmpty() ? "No appointments for " + patientName : formatAppointments(apps);
    }

    // 3. All doctor full schedule (free + booked)
    @Override
    public String getAllDoctorsFullSchedule() {
        List<doctor_mod> docs = doctorRepo.findAll();
        if (docs.isEmpty()) return "X No doctors in system.";

        StringBuilder sb = new StringBuilder();
        for (doctor_mod d : docs) {
            List<Integer> free = getAvailableSlotsForDoctor(d.getD_spl());
            List<appointment_mod> booked = getDoctorSchedule(d.getD_spl());
            sb.append("👨‍⚕️ Dr. ").append(d.getD_name()).append(" (").append(d.getD_spl()).append(")\n")
                    .append("📅 Free: ").append(free).append("\n")
                    .append("📋 Booked: ").append(formatAppointments(booked)).append("\n\n");
        }
        return sb.toString();
    }

    // 4. Free slots by specialization (already exists)
    @Override
    public List<Integer> getAvailableSlots(String doctorType) {
        return getAvailableSlotsForDoctor(doctorType);
    }

    // 5. Full schedule by specialization (already exists)
    @Override
    public List<appointment_mod> getDoctorSchedule(String doctorType) {
        return appointmentRepo.findAll().stream()
                .filter(app -> app.getD_spl().equalsIgnoreCase(doctorType))
                .collect(Collectors.toList());
    }

}