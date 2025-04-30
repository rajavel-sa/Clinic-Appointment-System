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

    // 🔵 Book appointment (doctorName, doctorType, time)
    @PostMapping("/book")
    public String bookAppointment(@RequestBody Map<String, Object> body) {
        String doctorType = (String) body.get("doctorType");
        String patientName = (String) body.get("patientName");
        int time = (Integer) body.get("time");

        return clinicServ.bookAppointment(doctorType, patientName, time);
    }

    // 🟡 Reschedule appointment (doctorType, patientName, newTime)
    @PostMapping("/reschedule")
    public String rescheduleAppointment(@RequestBody Map<String, Object> body) {
        String doctorType = (String) body.get("doctorType");
        String patientName = (String) body.get("patientName");
        int time = (Integer) body.get("time");

        return clinicServ.rescheduleAppointment(doctorType, patientName, time);
    }

    // 🔴 Cancel appointment (patientName, time)
    @DeleteMapping("/cancel")
    public String cancelAppointment(@RequestBody Map<String, Object> body) {
        String patientName = (String) body.get("patientName");
        int time = (Integer) body.get("time");

        return clinicServ.cancelAppointment(patientName, time);
    }

    // 1️⃣ Get full schedule for all patients
    @GetMapping("/patients/schedule")
    public String getAllPatientsSchedule() {
        return clinicServ.getAllPatientsSchedule();
    }

    // 2️⃣ Get a specific patient’s schedule
    @GetMapping("/patient/{patientName}")
    public String getPatientSchedule(@PathVariable String patientName) {
        return clinicServ.getAppointmentsForPatient(patientName);
    }

    // 3️⃣ Get all doctors' free and booked time
    @GetMapping("/doctors/schedule")
    public String getAllDoctorsFreeSlots() {
        return clinicServ.getAllDoctorsFreeSlots();
    }

    // 4️⃣ Get all free slots for a doctor type
    @GetMapping("/doctor-slots/{doctorType}")
    public List<Integer> getAvailableSlots(@PathVariable String doctorType) {
        return clinicServ.getAvailableSlots(doctorType);
    }

    // 5️⃣ Get full schedule of a specific doctor type
    @GetMapping("/doctor-schedule/{doctorType}")
    public List<appointment_mod> getDoctorSchedule(@PathVariable String doctorType) {
        return clinicServ.getDoctorSchedule(doctorType);
    }
}



/*
requests

for book i need to give 3 -docname,doctype,time
for reshed i need to give 3 - doc, pname,new time (3 conditoins- cant have the same doctor type appointment twice, can have differet doctor appintment at differnet time, )
for cancel - patient name and time is enough
for getting
1. need to display all the patients schedule
2. ned to disaplay the required patients schedule
3. need to diaply all the doctors schedule free time and schedule time
4. need to display all the spcilixed doctor (like ent dcotors ) free time and schedule
5. neeed to display the the requereied doctor scheduel

for now only one action can be done at a time menain ony for one person we can cancel in a request , one book , one reshedule
 */