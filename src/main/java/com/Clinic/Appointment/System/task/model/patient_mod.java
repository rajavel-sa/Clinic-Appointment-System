package com.Clinic.Appointment.System.task.model;

import jakarta.persistence.*;

@Entity
@Table(name="patient_tablee")
public class patient_mod {

    @Id
    private String p_name;
    private String p_need;

    public String getP_name() {
        return p_name;
    }

    public void setP_name(String p_name) {
        this.p_name = p_name;
    }

    public String getP_need() {
        return p_need;
    }

    public void setP_need(String p_need) {
        this.p_need = p_need;
    }

    public patient_mod() {}

    public patient_mod(String p_name, String p_need) {
        this.p_name = p_name;
        this.p_need = p_need;
    }
}