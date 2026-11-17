package br.com.serene;

import java.util.*;
import java.time.*;
public record ConsultationView(UUID id, UUID patientId, UUID professionalId, OffsetDateTime scheduledStartAt, OffsetDateTime scheduledEndAt, String status, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
    public static ConsultationView from(Consultation entity) {
        return new ConsultationView(entity.id, entity.patientId, entity.professionalId, entity.scheduledStartAt, entity.scheduledEndAt, entity.status, entity.createdAt, entity.updatedAt);
    }
}
