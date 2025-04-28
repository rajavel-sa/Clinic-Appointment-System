package com.Clinic.Appointment.System.task.service;
import com.Clinic.Appointment.System.task.model.appointment_mod;
import com.Clinic.Appointment.System.task.model.doctor_mod;
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

    ////////////////////////////////////////////////

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

    private boolean isValidTimeSlot(int time) {
        return validSlots.contains(time);
    }

    private Optional<appointment_mod> getExistingAppointment(String patientName) {
        return appointmentRepo.findAll()
                .stream()
                .filter(app -> app.getP_name().equalsIgnoreCase(patientName))
                .findFirst();
    }

    private boolean isTimeAlreadyBooked(String doctorType, int time) {
        return appointmentRepo.findAll()
                .stream()
                .anyMatch(app -> app.getD_spl().equalsIgnoreCase(doctorType) && app.getTime() == time);
    }

    /// /////////////////////////////////////////////


    @Override
    public String bookAppointment(String doctorType, String patientName, int time) {
        Optional<doctor_mod> doctorOpt = doctorRepo.findAll()
                .stream()
                .filter(doc -> doc.getD_spl().equalsIgnoreCase(doctorType))
                .findFirst();

        if (doctorOpt.isEmpty()) {
            throw new IllegalArgumentException("X No doctor found with the specialization: " + doctorType);
        }

        doctor_mod doctor = doctorOpt.get();
        List<Integer> availableSlots = getAvailableSlotsForDoctor(doctorType);

        if (!isValidTimeSlot(time)) {
            throw new IllegalArgumentException("X Invalid time slot: " + time + ". Available slots for " + doctor.getD_spl() + " Dr. " + doctor.getD_name() + " are: " + availableSlots);
        }

        List<appointment_mod> existingAppointments = appointmentRepo.findAll()
                .stream()
                .filter(app -> app.getP_name().equalsIgnoreCase(patientName))
                .collect(Collectors.toList());

        for (appointment_mod existingAppointment : existingAppointments) {
            if (existingAppointment.getD_spl().equalsIgnoreCase(doctorType)) {
                throw new IllegalArgumentException("X Patient " + patientName + " already has an appointment with "
                        + existingAppointment.getD_spl() + " Dr. " + existingAppointment.getD_name()
                        + " at time slot " + existingAppointment.getTime() + ". Cannot book same doctor again.");
            }
            if (existingAppointment.getTime() == time) {
                throw new IllegalArgumentException("X Patient " + patientName + " already has an appointment at time slot "
                        + existingAppointment.getTime() + " with " + existingAppointment.getD_spl()
                        + " Dr. " + existingAppointment.getD_name() + ". Cannot book same time with different doctor.");
            }
        }

        if (isTimeAlreadyBooked(doctorType, time)) {
            throw new IllegalArgumentException(" !! Time slot " + time + " already booked for Dr. " + doctor.getD_name() + ". Available slots: " + availableSlots);
        }

        appointment_mod appointment = new appointment_mod();
        appointment.setD_name(doctor.getD_name());
        appointment.setD_spl(doctor.getD_spl());
        appointment.setP_name(patientName);
        appointment.setTime(time);
        appointmentRepo.save(appointment);

        return "Done. Appointment booked for " + patientName + " with " + doctor.getD_spl() + " Dr. " + doctor.getD_name() + " at time slot " + time + ".";
    }

    @Override
    public String rescheduleAppointment(String doctorType, String patientName, int time) {
        Optional<appointment_mod> existingOpt = appointmentRepo.findAll()
                .stream()
                .filter(app -> app.getP_name().equalsIgnoreCase(patientName) && app.getD_spl().equalsIgnoreCase(doctorType))
                .findFirst();

        if (existingOpt.isEmpty()) {
            return "X No existing appointment found for patient: " + patientName + " with " + doctorType;
        }

        Optional<doctor_mod> doctorOpt = doctorRepo.findAll()
                .stream()
                .filter(doc -> doc.getD_spl().equalsIgnoreCase(doctorType))
                .findFirst();

        if (doctorOpt.isEmpty()) {
            return "X No doctor found with the specialization: " + doctorType;
        }

        List<Integer> availableSlots = getAvailableSlotsForDoctor(doctorType);

        if (!isValidTimeSlot(time)) {
            return "X Invalid time slot: " + time + ". Available slots for " + doctorType + " are: " + availableSlots;
        }

        if (isTimeAlreadyBooked(doctorType, time)) {
            return "! Time slot " + time + " is already booked. Available slots for " + doctorType + " are: " + availableSlots;
        }

        // Get the old time before updating
        appointment_mod appointment = existingOpt.get();
        int oldTime = appointment.getTime();  // Save the old time slot

        // Update the appointment with the new time
        appointment.setTime(time);
        appointmentRepo.save(appointment);

        // Return a response with details
        return "✅ Appointment for patient " + patientName + " with " + doctorOpt.get().getD_spl() + " Dr. " + doctorOpt.get().getD_name()
                + " rescheduled from time slot " + oldTime + " to " + time + ".";
    }

    @Override
    public String cancelAppointment(String patientName, int time) {
    List<appointment_mod> appointments = appointmentRepo.findAll()
            .stream()
            .filter(app -> app.getP_name().equalsIgnoreCase(patientName)
                    && app.getTime() == time)
            .collect(Collectors.toList());

    if (appointments.isEmpty()) {
        return "X No appointment found for patient " + patientName + " at time " + time;
    }

    appointment_mod appointment = appointments.get(0);
    String doctorName = appointment.getD_name();
    String specialization = appointment.getD_spl();
    appointmentRepo.delete(appointment);

    List<appointment_mod> remainingAppointments = appointmentRepo.findAll()
            .stream()
            .filter(app -> app.getP_name().equalsIgnoreCase(patientName))
            .collect(Collectors.toList());

    StringBuilder remainingAppointmentsDetails = new StringBuilder();
    for (appointment_mod app : remainingAppointments) {
        remainingAppointmentsDetails.append("Doctor: ").append(app.getD_name())
                .append(", Specialization: ").append(app.getD_spl())
                .append(", Time: ").append(app.getTime())
                .append("\n");
    }

    return "✅ Appointment for patient " + patientName + " with specialization " + specialization +
            " Dr. " + doctorName + " at time " + time + " is deleted.\n" +
            (remainingAppointments.isEmpty() ? "No remaining appointments." :
                    "Remaining appointments:\n" + remainingAppointmentsDetails.toString());
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
        // First check if patient exists
        boolean patientExists = patientRepo.findAll()
                .stream()
                .anyMatch(p -> p.getP_name().equalsIgnoreCase(patientName));

        if (!patientExists) {
            return "X No patient found with name: " + patientName + ".";
        }

        // Fetch all appointments for the patient
        List<appointment_mod> appointments = appointmentRepo.findAll()
                .stream()
                .filter(app -> app.getP_name().equalsIgnoreCase(patientName))
                .sorted(Comparator.comparingInt(appointment_mod::getTime)) // Sort by time
                .toList();

        if (appointments.isEmpty()) {
            return "! Patient " + patientName + " is registered but has no appointments yet.";
        }

        String result = " Appointments for patient " + patientName + " (" + appointments.size() + " total):\n";
        for (appointment_mod app : appointments) {
            result += " # " + app.getD_spl() + " Dr. " + app.getD_name()
                    + " at time " + app.getTime() + ".\n";
        }

        return result;
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
