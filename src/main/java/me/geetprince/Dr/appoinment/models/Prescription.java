package me.geetprince.Dr.appoinment.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "prescriptions")
public class Prescription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "appointment_id", nullable = false, unique = true)
    private Appointment appointment;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String diagnosis;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String medicines;

    @Column(columnDefinition = "TEXT")
    private String instructions;

    @Column(nullable = false, updatable = false)
    private LocalDateTime prescribedAt;

    @PrePersist
    protected void onCreate() {
        this.prescribedAt = LocalDateTime.now();
    }

    public Prescription() {
    }

    public Prescription(Long id, Appointment appointment, String diagnosis,
                        String medicines, String instructions, LocalDateTime prescribedAt) {
        this.id = id;
        this.appointment = appointment;
        this.diagnosis = diagnosis;
        this.medicines = medicines;
        this.instructions = instructions;
        this.prescribedAt = prescribedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Appointment getAppointment() {
        return appointment;
    }

    public void setAppointment(Appointment appointment) {
        this.appointment = appointment;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public String getMedicines() {
        return medicines;
    }

    public void setMedicines(String medicines) {
        this.medicines = medicines;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public LocalDateTime getPrescribedAt() {
        return prescribedAt;
    }

    public void setPrescribedAt(LocalDateTime prescribedAt) {
        this.prescribedAt = prescribedAt;
    }
}
