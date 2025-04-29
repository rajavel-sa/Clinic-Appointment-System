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
    public String bookAppointment(String doctorType, String patientName, int time) {

        if (!patientExists(patientName)) {
            patient_mod newPatient = new patient_mod();
            newPatient.setP_name(patientName);
            newPatient.setP_need(doctorType);
            patientRepo.save(newPatient);
        }

        Optional<doctor_mod> doctorOpt = getDoctorByType(doctorType);
        if (doctorOpt.isEmpty()) {
            throw new IllegalArgumentException("X No doctor found with the specialization: " + doctorType);
        }

        long appointmentsForDoctor = appointmentRepo.findAll()
                .stream()
                .filter(app -> app.getD_spl().equalsIgnoreCase(doctorType))
                .count();

        if (appointmentsForDoctor >= 3) {
            throw new IllegalStateException("Doctor already has maximum appointments for the day.");
        }

        List<Integer> availableSlots = getAvailableSlotsForDoctor(doctorType);

        if (!isValidTimeSlot(time)) {
            throw new IllegalArgumentException("X Invalid time slot: " + time + ". Available slots: " + availableSlots);
        }

        List<appointment_mod> existingAppointments = fetchAppointmentsByPatient(patientName);

        for (appointment_mod existing : existingAppointments) {
            if (existing.getD_spl().equalsIgnoreCase(doctorType)) {
                throw new IllegalArgumentException("X Patient " + patientName + " already has an appointment with "
                        + existing.getD_spl() + " Dr. " + existing.getD_name()
                        + " at time slot " + existing.getTime() + ". Cannot book same doctor again.");
            }
            if (existing.getTime() == time) {
                throw new IllegalArgumentException("X Patient " + patientName + " already has an appointment at time "
                        + existing.getTime() + " with " + existing.getD_spl()
                        + " Dr. " + existing.getD_name() + ". Cannot book same time with different doctor.");
            }
        }

        if (isTimeAlreadyBooked(doctorType, time)) {
            throw new IllegalArgumentException("!! Time slot " + time + " already booked. Available: " + availableSlots);
        }

        appointment_mod appointment = new appointment_mod();
        appointment.setD_name(doctorOpt.get().getD_name());
        appointment.setD_spl(doctorOpt.get().getD_spl());
        appointment.setP_name(patientName);
        appointment.setTime(time);
        appointmentRepo.save(appointment);

        return "Done. Appointment booked for " + patientName + " with " + doctorOpt.get().getD_spl() + " Dr. " + doctorOpt.get().getD_name() + " at time slot " + time + ".";
    }

    @Override
    public String rescheduleAppointment(String doctorType, String patientName, int time) {
        Optional<appointment_mod> existingOpt = appointmentRepo.findAll()
                .stream()
                .filter(app -> app.getP_name().equalsIgnoreCase(patientName)
                        && app.getD_spl().equalsIgnoreCase(doctorType))
                .findFirst();

        if (existingOpt.isEmpty()) {
            return "X No existing appointment found for patient: " + patientName + " with " + doctorType;
        }

        Optional<doctor_mod> doctorOpt = getDoctorByType(doctorType);

        if (doctorOpt.isEmpty()) {
            return "X No doctor found with the specialization: " + doctorType;
        }

        if (!isValidTimeSlot(time)) {
            return "X Invalid time slot: " + time + ". Available slots: " + getAvailableSlotsForDoctor(doctorType);
        }

        if (isTimeAlreadyBooked(doctorType, time)) {
            return "! Time slot " + time + " already booked. Available: " + getAvailableSlotsForDoctor(doctorType);
        }

        appointment_mod appointment = existingOpt.get();
        int oldTime = appointment.getTime();
        appointment.setTime(time);
        appointmentRepo.save(appointment);

        return "✅ Appointment for patient " + patientName + " with " + doctorType
                + " Dr. " + doctorOpt.get().getD_name()
                + " rescheduled from " + oldTime + " to " + time + ".";
    }

    @Override
    public String cancelAppointment(String patientName, int time) {
        List<appointment_mod> matchingAppointments = appointmentRepo.findAll()
                .stream()
                .filter(app -> app.getP_name().equalsIgnoreCase(patientName)
                        && app.getTime() == time)
                .collect(Collectors.toList());

        if (matchingAppointments.isEmpty()) {
            List<appointment_mod> allAppointments = fetchAppointmentsByPatient(patientName);

            if (allAppointments.isEmpty()) {
                return "X No appointment found for patient '" + patientName + "' at time " + time + ", and the patient has no other appointments either.";
            }

            return "X No appointment found for patient '" + patientName + "' at time " + time + ".\n But they have other appointments:\n" +
                    formatAppointments(allAppointments);
        }

        appointment_mod appointment = matchingAppointments.get(0);
        appointmentRepo.delete(appointment);

        List<appointment_mod> remainingAppointments = fetchAppointmentsByPatient(patientName);

        return "Done. Appointment for patient '" + patientName + "' with Dr. " + appointment.getD_name() +
                " (" + appointment.getD_spl() + ") at time " + time + " is cancelled.\n" +
                (remainingAppointments.isEmpty() ? "No remaining appointments." : "Remaining appointments:\n" + formatAppointments(remainingAppointments));
    }

    @Override
    public List<Integer> getAvailableSlots(String doctorType) {
        return getAvailableSlotsForDoctor(doctorType);
    }

    @Override
    public List<appointment_mod> getDoctorSchedule(String doctorType) {
        return appointmentRepo.findAll()
                .stream()
                .filter(app -> app.getD_spl().equalsIgnoreCase(doctorType))
                .collect(Collectors.toList());
    }

    @Override
    public String getAppointmentsForPatient(String patientName) {
        if (!patientExists(patientName)) {
            return "X No patient found with name: " + patientName + ".";
        }

        List<appointment_mod> appointments = fetchAppointmentsByPatient(patientName);

        if (appointments.isEmpty()) {
            return "! Patient " + patientName + " is registered but has no appointments yet.";
        }

        return " Appointments for patient " + patientName + " (" + appointments.size() + " total):\n" +
                formatAppointments(appointments);
    }

    @Override
    public String getAllDoctorsFreeSlots() {
        List<doctor_mod> doctors = doctorRepo.findAll();

        if (doctors.isEmpty()) {
            return "X No doctors found.";
        }

        StringBuilder result = new StringBuilder();
        for (doctor_mod doctor : doctors) {
            List<Integer> availableSlots = getAvailableSlotsForDoctor(doctor.getD_spl());
            result.append("️ Dr. ").append(doctor.getD_name())
                    .append(" (").append(doctor.getD_spl()).append(") - Available Slots: ")
                    .append(availableSlots.isEmpty() ? "X No slots available" : availableSlots.toString())
                    .append("\n");
        }
        return result.toString();
    }


}