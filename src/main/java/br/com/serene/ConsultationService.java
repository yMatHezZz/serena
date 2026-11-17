package br.com.serene;

import java.util.*;
import java.time.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.*;
@Service
@Transactional
public class ConsultationService {
    private final ConsultationRepository repository;
    private final SchedulingRules rules;
    public ConsultationService(ConsultationRepository repository, SchedulingRules rules) {
        this.repository = repository; this.rules = rules;
    }
    @Transactional(readOnly=true)
    public Page<ConsultationView> list(int page, int size) {
        if (page < 0 || size < 1 || size > 100) throw ApiException.badRequest("Use page >= 0 e size entre 1 e 100.");
        return repository.findAll(PageRequest.of(page, size, Sort.by("createdAt").ascending().and(Sort.by("id")))).map(ConsultationView::from);
    }
    @Transactional(readOnly=true)
    public ConsultationView get(UUID id) { return ConsultationView.from(find(id)); }
    private Consultation find(UUID id) { return repository.findById(id).orElseThrow(() -> ApiException.notFound("Registro não encontrado.")); }
    public ConsultationView create(ConsultationRequest request) { return save(new Consultation(), request); }
    public ConsultationView update(UUID id, ConsultationRequest request) { return save(find(id), request); }
    private ConsultationView save(Consultation entity, ConsultationRequest request) {
        rules.validate(entity, request);
        entity.patientId = request.patientId();
        entity.professionalId = request.professionalId();
        entity.scheduledStartAt = request.scheduledStartAt();
        entity.scheduledEndAt = request.scheduledEndAt();
        entity.status = request.status();
        return ConsultationView.from(repository.saveAndFlush(entity));
    }
    public void delete(UUID id) { var entity = find(id); rules.beforeDelete(entity); repository.delete(entity); repository.flush(); }
}
