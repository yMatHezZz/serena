package br.com.serene;

import java.time.OffsetDateTime;
import java.util.*;
import org.springframework.stereotype.Component;

/** Regras compartilhadas; executadas dentro da transação dos services. */
@Component
public class SchedulingRules {
    private final UserRepository users;
    private final PatientRepository patients;
    private final ProfessionalRepository professionals;
    private final AvailabilitySlotRepository slots;
    private final ConsultationRepository consultations;
    public SchedulingRules(UserRepository users, PatientRepository patients, ProfessionalRepository professionals,
            AvailabilitySlotRepository slots, ConsultationRepository consultations) {
        this.users = users; this.patients = patients; this.professionals = professionals;
        this.slots = slots; this.consultations = consultations;
    }
    public void validate(User entity, UserRequest r) { }
    public void validate(Activity entity, ActivityRequest r) {
        try {
            if (new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsBytes(r.configuration()).length > 16384)
                throw ApiException.badRequest("configuration deve ter até 16 KiB.");
        } catch (com.fasterxml.jackson.core.JsonProcessingException ex) { throw ApiException.badRequest("configuration inválida."); }
    }
    public void validate(Patient entity, PatientRequest r) {
        immutable(entity.userId, r.userId()); lockUsers(r.userId());
    }
    public void validate(Professional entity, ProfessionalRequest r) {
        immutable(entity.userId, r.userId()); lockUsers(r.userId());
    }
    public void validate(AvailabilitySlot entity, AvailabilitySlotRequest r) {
        immutable(entity.professionalId, r.professionalId());
        var professional = professional(r.professionalId());
        lockUsers(professional.userId);
        interval(r.startAt(), r.endAt());
        if (entity.id == null || !same(entity.startAt, r.startAt()) || !same(entity.endAt, r.endAt())) future(r.startAt());
        if (entity.id != null) ensureBookingsCovered(entity, r.startAt(), r.endAt(), r.available());
    }
    public void validate(Consultation entity, ConsultationRequest r) {
        immutable(entity.patientId, r.patientId()); immutable(entity.professionalId, r.professionalId());
        var patient = patients.findById(r.patientId()).orElseThrow(() -> ApiException.notFound("Paciente não encontrado."));
        var professional = professional(r.professionalId());
        if (patient.userId.equals(professional.userId)) throw ApiException.conflict("Profissional não pode consultar a si mesmo.");
        lockUsers(patient.userId, professional.userId);
        interval(r.scheduledStartAt(), r.scheduledEndAt());
        if (entity.id == null && !"SCHEDULED".equals(r.status())) throw ApiException.badRequest("Nova consulta deve ter status SCHEDULED.");
        if (entity.id != null && !"SCHEDULED".equals(entity.status))
            throw ApiException.conflict("Consulta encerrada não pode ser alterada.");
        boolean moved = entity.id == null || !same(entity.scheduledStartAt, r.scheduledStartAt()) || !same(entity.scheduledEndAt, r.scheduledEndAt());
        if (!"SCHEDULED".equals(r.status()) && moved) throw ApiException.badRequest("Não altere horários ao encerrar uma consulta.");
        if ("COMPLETED".equals(r.status()) && r.scheduledEndAt().isAfter(OffsetDateTime.now()))
            throw ApiException.conflict("Só é possível concluir após o término previsto.");
        if ("SCHEDULED".equals(r.status())) {
            if (moved) future(r.scheduledStartAt());
            active(patient.userId); active(professional.userId);
            if (slots.covering(r.professionalId(), r.scheduledStartAt(), r.scheduledEndAt()).isEmpty())
                throw ApiException.conflict("Consulta deve estar contida em um horário disponível do profissional.");
        }
        // Exclusion constraints no PostgreSQL bloqueiam sobreposição inclusive entre transações concorrentes.
    }
    public void beforeDelete(BaseEntity entity) {
        if (entity instanceof AvailabilitySlot slot) {
            lockUsers(professional(slot.professionalId).userId);
            ensureBookingsCovered(slot, slot.startAt, slot.endAt, false);
        }
        if (entity instanceof Consultation consultation) {
            var patient = patients.findById(consultation.patientId).orElseThrow();
            lockUsers(patient.userId, professional(consultation.professionalId).userId);
        }
        // FKs RESTRICT impedem apagar usuários/perfis com dependências; sem cascata silenciosa.
    }
    private void ensureBookingsCovered(AvailabilitySlot slot, OffsetDateTime start, OffsetDateTime end, boolean available) {
        for (var c : consultations.scheduledFor(slot.professionalId)) {
            boolean wasCovered = !c.scheduledStartAt.isBefore(slot.startAt) && !c.scheduledEndAt.isAfter(slot.endAt);
            if (wasCovered && (!available || c.scheduledStartAt.isBefore(start) || c.scheduledEndAt.isAfter(end)))
                throw ApiException.conflict("Há consulta agendada neste horário. Cancele ou reagende primeiro.");
        }
    }
    private Professional professional(UUID id) {
        return professionals.findById(id).orElseThrow(() -> ApiException.notFound("Profissional não encontrado."));
    }
    private void lockUsers(UUID... ids) {
        Arrays.stream(ids).distinct().sorted().forEach(id -> users.lockById(id)
            .orElseThrow(() -> ApiException.notFound("Usuário não encontrado.")));
    }
    private void active(UUID id) {
        if (!"ACTIVE".equals(users.findById(id).orElseThrow().status)) throw ApiException.conflict("Usuário inativo não pode agendar consulta.");
    }
    private void immutable(UUID oldId, UUID newId) {
        if (oldId != null && !oldId.equals(newId)) throw ApiException.conflict("O vínculo não pode ser trocado; crie um novo registro.");
    }
    private void interval(OffsetDateTime start, OffsetDateTime end) {
        if (!end.isAfter(start)) throw ApiException.badRequest("O término deve ser posterior ao início.");
    }
    private void future(OffsetDateTime start) {
        if (!start.isAfter(OffsetDateTime.now())) throw ApiException.badRequest("O início deve estar no futuro.");
    }
    private boolean same(OffsetDateTime a, OffsetDateTime b) { return a != null && a.isEqual(b); }
}
