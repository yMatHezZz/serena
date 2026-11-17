package br.com.serene;

import java.util.*;
import java.time.*;
import jakarta.validation.constraints.*;
public record ActivityRequest(
    @NotBlank @Size(max=100) String name,
    @Size(max=5000) String description,
    @NotBlank @Size(max=50) String activityType,
    @NotNull Map<String, Object> configuration,
    @NotNull Boolean active
) {}
