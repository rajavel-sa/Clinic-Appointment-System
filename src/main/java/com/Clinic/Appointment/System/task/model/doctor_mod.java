package com.Clinic.Appointment.System.task.model;

import jakarta.persistence.*;

@Entity
@Table(name="doctor_tablee")
public class doctor_mod {

    @Id
    private String d_name;
    private String d_spl;

    public String getD_name() {
        return d_name;
    }

    public void setD_name(String d_name) {
        this.d_name = d_name;
    }

    public String getD_spl() {
        return d_spl;
    }

    public void setD_spl(String d_spl) {
        this.d_spl = d_spl;
    }

    public doctor_mod(){}

    public doctor_mod(String d_name, String d_spl) {
        this.d_name = d_name;
        this.d_spl = d_spl;
    }
}
