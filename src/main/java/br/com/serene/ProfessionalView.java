package br.com.serene;

import java.util.*;
import java.time.*;
public record ProfessionalView(UUID id, UUID userId, String licenseNumber, String specialty, String bio, String phone, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
    public static ProfessionalView from(Professional entity) {
        return new ProfessionalView(entity.id, entity.userId, entity.licenseNumber, entity.specialty, entity.bio, entity.phone, entity.createdAt, entity.updatedAt);
    }
}
