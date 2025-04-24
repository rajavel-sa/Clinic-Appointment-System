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

    @GetMapping("/available-slots/{doctorType}")
    public List<Integer> getAvailableSlots(@PathVariable String doctorType) {
        return clinicServ.getAvailableSlots(doctorType);
    }

//    @GetMapping("/available-slots/{doctorType}")
//    public List<Integer> getAvailableSlots(@PathVariable String doctorType) {
//        return clinicServ.getAvailableSlots(doctorType);
//    }

//    @GetMapping("/doctor-schedule/{doctorType}")
//    public List<appointment_mod> getDoctorSchedule(@PathVariable String doctorType) {
//        return clinicServ.getDoctorSchedule(doctorType);
//    }


}