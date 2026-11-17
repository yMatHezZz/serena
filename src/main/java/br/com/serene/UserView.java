package br.com.serene;

import java.util.*;
import java.time.*;
public record UserView(UUID id, String name, String email, String status, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
    public static UserView from(User entity) {
        return new UserView(entity.id, entity.name, entity.email, entity.status, entity.createdAt, entity.updatedAt);
    }
}
