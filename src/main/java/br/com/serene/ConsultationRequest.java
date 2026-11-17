package br.com.serene;

import java.util.*;
import java.time.*;
import jakarta.validation.constraints.*;
public record ConsultationRequest(
    @NotNull UUID patientId,
    @NotNull UUID professionalId,
    @NotNull OffsetDateTime scheduledStartAt,
    @NotNull OffsetDateTime scheduledEndAt,
    @NotNull @Pattern(regexp="SCHEDULED|CANCELLED|COMPLETED") String status
) {}
