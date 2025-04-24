package com.Clinic.Appointment.System.task.model;

import jakarta.persistence.*;

@Entity
@Table(name="appointment_table")
public class appointment_mod {

    @Id
    private int appointment_id;
    private String d_name;
    private String d_spl;
    private String p_name;
    private String time;

    public int getAppointment_id() {
        return appointment_id;
    }

    public void setAppointment_id(int appointment_id) {
        this.appointment_id = appointment_id;
    }

    public String getD_spl() {
        return d_spl;
    }

    public void setD_spl(String d_spl) {
        this.d_spl = d_spl;
    }

    public String getD_name() {
        return d_name;
    }

    public void setD_name(String d_name) {
        this.d_name = d_name;
    }

    public String getP_name() {
        return p_name;
    }

    public void setP_name(String p_name) {
        this.p_name = p_name;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public appointment_mod() {}

    public appointment_mod(int appointment_id, String time, String p_name, String d_spl, String d_name) {
        this.appointment_id = appointment_id;
        this.time = time;
        this.p_name = p_name;
        this.d_spl = d_spl;
        this.d_name = d_name;
    }
}
