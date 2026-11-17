package br.com.serene;

import jakarta.persistence.*;
import java.util.*;
import java.time.*;
@Entity @Table(name="availability_slots")
public class AvailabilitySlot extends BaseEntity {
    @Column(name="professional_id", columnDefinition="uuid") public UUID professionalId;
    @Column(name="start_at", columnDefinition="timestamptz") public OffsetDateTime startAt;
    @Column(name="end_at", columnDefinition="timestamptz") public OffsetDateTime endAt;
    @Column(name="is_available", columnDefinition="boolean") public Boolean available;
}
