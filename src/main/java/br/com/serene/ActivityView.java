package br.com.serene;

import java.util.*;
import java.time.*;
public record ActivityView(UUID id, String name, String description, String activityType, Map<String, Object> configuration, Boolean active, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
    public static ActivityView from(Activity entity) {
        return new ActivityView(entity.id, entity.name, entity.description, entity.activityType, entity.configuration, entity.active, entity.createdAt, entity.updatedAt);
    }
}
