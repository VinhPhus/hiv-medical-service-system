package com.hivcare.dto.request;



public class AppointmentRequest {
    
    @NotNull
    private Long doctorId;

    @NotNull
    @Future
    private LocalDateTime appointmentDate;

    private Appointment.AppointmentType appointmentType = Appointment.AppointmentType.CONSULTATION;

    private String reason;

    private boolean anonymous = false;

    private boolean online = false;

    public Long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }

    public LocalDateTime getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(LocalDateTime appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public Appointment.AppointmentType getAppointmentType() {
        return appointmentType;
    }

    public void setAppointmentType(Appointment.AppointmentType appointmentType) {
        this.appointmentType = appointmentType;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public boolean isAnonymous() {
        return anonymous;
    }

    public void setAnonymous(boolean anonymous) {
        this.anonymous = anonymous;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }
}
