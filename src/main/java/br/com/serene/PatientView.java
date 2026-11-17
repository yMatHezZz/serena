package br.com.serene;

import java.util.*;
import java.time.*;
public record PatientView(UUID id, UUID userId, LocalDate dateOfBirth, String gender, String phone, String address, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
    public static PatientView from(Patient entity) {
        return new PatientView(entity.id, entity.userId, entity.dateOfBirth, entity.gender, entity.phone, entity.address, entity.createdAt, entity.updatedAt);
    }
}
