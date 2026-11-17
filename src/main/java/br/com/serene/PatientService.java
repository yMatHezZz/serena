package br.com.serene;

import java.util.*;
import java.time.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.*;
@Service
@Transactional
public class PatientService {
    private final PatientRepository repository;
    private final SchedulingRules rules;
    public PatientService(PatientRepository repository, SchedulingRules rules) {
        this.repository = repository; this.rules = rules;
    }
    @Transactional(readOnly=true)
    public Page<PatientView> list(int page, int size) {
        if (page < 0 || size < 1 || size > 100) throw ApiException.badRequest("Use page >= 0 e size entre 1 e 100.");
        return repository.findAll(PageRequest.of(page, size, Sort.by("createdAt").ascending().and(Sort.by("id")))).map(PatientView::from);
    }
    @Transactional(readOnly=true)
    public PatientView get(UUID id) { return PatientView.from(find(id)); }
    private Patient find(UUID id) { return repository.findById(id).orElseThrow(() -> ApiException.notFound("Registro não encontrado.")); }
    public PatientView create(PatientRequest request) { return save(new Patient(), request); }
    public PatientView update(UUID id, PatientRequest request) { return save(find(id), request); }
    private PatientView save(Patient entity, PatientRequest request) {
        rules.validate(entity, request);
        entity.userId = request.userId();
        entity.dateOfBirth = request.dateOfBirth();
        entity.gender = request.gender();
        entity.phone = request.phone();
        entity.address = request.address();
        return PatientView.from(repository.saveAndFlush(entity));
    }
    public void delete(UUID id) { var entity = find(id); rules.beforeDelete(entity); repository.delete(entity); repository.flush(); }
}
