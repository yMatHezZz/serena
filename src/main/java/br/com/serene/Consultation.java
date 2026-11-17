package br.com.serene;

import jakarta.persistence.*;
import java.util.*;
import java.time.*;
@Entity @Table(name="consultations")
public class Consultation extends BaseEntity {
    @Column(name="patient_id", columnDefinition="uuid") public UUID patientId;
    @Column(name="professional_id", columnDefinition="uuid") public UUID professionalId;
    @Column(name="scheduled_start_at", columnDefinition="timestamptz") public OffsetDateTime scheduledStartAt;
    @Column(name="scheduled_end_at", columnDefinition="timestamptz") public OffsetDateTime scheduledEndAt;
    @Column(name="status", columnDefinition="varchar(20)") public String status;
}
