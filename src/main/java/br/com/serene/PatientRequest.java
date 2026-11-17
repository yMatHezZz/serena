package br.com.serene;

import java.util.*;
import java.time.*;
import jakarta.validation.constraints.*;
public record PatientRequest(
    @NotNull UUID userId,
    @NotNull @Past LocalDate dateOfBirth,
    @Size(max=20) String gender,
    @Size(max=20) String phone,
    @Size(max=1000) String address
) {}
