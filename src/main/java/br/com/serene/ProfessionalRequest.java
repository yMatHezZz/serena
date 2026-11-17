package br.com.serene;

import java.util.*;
import java.time.*;
import jakarta.validation.constraints.*;
public record ProfessionalRequest(
    @NotNull UUID userId,
    @NotBlank @Size(max=50) String licenseNumber,
    @NotBlank @Size(max=100) String specialty,
    @Size(max=5000) String bio,
    @Size(max=20) String phone
) {}
