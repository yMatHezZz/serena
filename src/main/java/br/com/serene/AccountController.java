package br.com.serene;

import java.time.LocalDate;
import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.core.Authentication;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@RestController
public class AccountController {
    public record PatientProfileRequest(@NotNull @Past LocalDate dateOfBirth, @Size(max=20) String gender,
        @Size(max=20) String phone, @Size(max=1000) String address) { }
    public record ProfessionalProfileRequest(@NotBlank @Size(max=50) String licenseNumber,
        @NotBlank @Size(max=100) String specialty, @Size(max=5000) String bio, @Size(max=20) String phone) { }
    public record AccountView(AccountService.Identity account, Object profile, List<ConsultationView> consultations) { }
    private final AccountService accounts;
    private final PatientRepository patients;
    private final ProfessionalRepository professionals;
    private final ConsultationRepository consultations;
    private final PatientService patientService;
    private final ProfessionalService professionalService;
    public AccountController(AccountService accounts, PatientRepository patients, ProfessionalRepository professionals,
            ConsultationRepository consultations, PatientService patientService, ProfessionalService professionalService) {
        this.accounts=accounts;this.patients=patients;this.professionals=professionals;this.consultations=consultations;
        this.patientService=patientService;this.professionalService=professionalService;
    }
    @PostMapping("/api/auth/register")
    public ResponseEntity<AccountService.Identity> register(@Valid @RequestBody RegistrationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(accounts.register(request));
    }
    @GetMapping("/api/auth/me")
    public AccountService.Identity me(Authentication auth) { return accounts.identity(auth); }
    @GetMapping("/api/account")
    public AccountView account(Authentication auth) {
        var me=accounts.identity(auth);
        var page=PageRequest.of(0,100,Sort.by("scheduledStartAt").descending());
        if (me.role().equals("PATIENT")) {
            var p=patients.findByUserId(me.id());
            return new AccountView(me,p.map(PatientView::from).orElse(null),p.map(v->consultations.findByPatientId(v.id,page).stream().map(ConsultationView::from).toList()).orElse(List.of()));
        }
        var p=professionals.findByUserId(me.id());
        return new AccountView(me,p.map(ProfessionalView::from).orElse(null),p.map(v->consultations.findByProfessionalId(v.id,page).stream().map(ConsultationView::from).toList()).orElse(List.of()));
    }
    @PutMapping("/api/account/patient-profile")
    public PatientView patient(Authentication auth, @Valid @RequestBody PatientProfileRequest request) {
        var me=accounts.identity(auth);requireRole(me,"PATIENT");
        var body=new PatientRequest(me.id(),request.dateOfBirth(),request.gender(),request.phone(),request.address());
        return patients.findByUserId(me.id()).map(p->patientService.update(p.id,body)).orElseGet(()->patientService.create(body));
    }
    @PutMapping("/api/account/professional-profile")
    public ProfessionalView professional(Authentication auth, @Valid @RequestBody ProfessionalProfileRequest request) {
        var me=accounts.identity(auth);requireRole(me,"PROFESSIONAL");
        var body=new ProfessionalRequest(me.id(),request.licenseNumber(),request.specialty(),request.bio(),request.phone());
        return professionals.findByUserId(me.id()).map(p->professionalService.update(p.id,body)).orElseGet(()->professionalService.create(body));
    }
    private void requireRole(AccountService.Identity me,String role) {
        if (!me.role().equals(role)) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }
}
