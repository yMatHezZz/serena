package br.com.serene;

import jakarta.validation.constraints.*;

public record RegistrationRequest(
    @NotBlank @Size(max=100) String name,
    @NotBlank @Email @Size(max=100) String email,
    @NotBlank @Size(min=12,max=72) String password,
    @NotNull @Pattern(regexp="PATIENT|PROFESSIONAL") String role,
    @NotNull @AssertTrue Boolean termsAccepted,
    @NotNull @AssertTrue Boolean privacyAccepted,
    @NotNull @AssertTrue Boolean consentAccepted
) { }
