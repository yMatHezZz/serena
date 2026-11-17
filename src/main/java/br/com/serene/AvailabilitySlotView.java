package br.com.serene;

import java.util.*;
import java.time.*;
public record AvailabilitySlotView(UUID id, UUID professionalId, OffsetDateTime startAt, OffsetDateTime endAt, Boolean available, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
    public static AvailabilitySlotView from(AvailabilitySlot entity) {
        return new AvailabilitySlotView(entity.id, entity.professionalId, entity.startAt, entity.endAt, entity.available, entity.createdAt, entity.updatedAt);
    }
}
