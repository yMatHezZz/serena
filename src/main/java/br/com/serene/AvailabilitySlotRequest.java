package br.com.serene;

import java.util.*;
import java.time.*;
import jakarta.validation.constraints.*;
public record AvailabilitySlotRequest(
    @NotNull UUID professionalId,
    @NotNull OffsetDateTime startAt,
    @NotNull OffsetDateTime endAt,
    @NotNull Boolean available
) {}
