package br.com.serene;

import java.util.*;
import java.time.*;
import jakarta.validation.constraints.*;
public record UserRequest(
    @NotBlank @Size(max=100) String name,
    @NotBlank @Email @Size(max=100) String email,
    @NotNull @Pattern(regexp="ACTIVE|INACTIVE") String status,
    @Size(min=12, max=72) String password
) {}
