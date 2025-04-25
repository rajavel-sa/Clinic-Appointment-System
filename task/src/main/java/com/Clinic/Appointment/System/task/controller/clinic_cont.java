package com.Clinic.Appointment.System.task.controller;

import com.Clinic.Appointment.System.task.model.appointment_mod;
import com.Clinic.Appointment.System.task.service.clinic_serv;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/clinic")
public class clinic_cont {

    private final clinic_serv clinicServ;

    public clinic_cont(clinic_serv clinicServ) {
        this.clinicServ = clinicServ;
    }

    @PostMapping("/book")
    public String bookAppointment(@RequestBody Map<String, Object> a) {
        String doctorType = (String) a.get("doctorType");
        String patientName = (String) a.get("patientName");
        int time = (int) a.get("time");

        return clinicServ.bookAppointment(doctorType, patientName, time);
    }

    @PostMapping("/reschedule")
    public String rescheduleAppointment(@RequestBody Map<String, Object> x) {
        String doctorType = (String) x.get("doctorType");
        String patientName = (String) x.get("patientName");
        int time = (int) x.get("time");

        return clinicServ.rescheduleAppointment(doctorType, patientName, time);
    }

    @DeleteMapping("/cancel/{patientName}")
    public String cancelAppointment(@PathVariable String patientName) {
        return clinicServ.cancelAppointment(patientName);
    }

    @GetMapping("/available-slots/{doctorType}")
    public List<Integer> getAvailableSlots(@PathVariable String doctorType) {
        return clinicServ.getAvailableSlots(doctorType);
    }

    @GetMapping("/doctor-schedule/{doctorType}")
    public List<appointment_mod> getDoctorSchedule(@PathVariable String doctorType) {
        return clinicServ.getDoctorSchedule(doctorType);
    }
}