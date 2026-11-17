package br.com.serene;

import java.util.*;
import java.time.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.*;
@Service
@Transactional
public class ProfessionalService {
    private final ProfessionalRepository repository;
    private final SchedulingRules rules;
    public ProfessionalService(ProfessionalRepository repository, SchedulingRules rules) {
        this.repository = repository; this.rules = rules;
    }
    @Transactional(readOnly=true)
    public Page<ProfessionalView> list(int page, int size) {
        if (page < 0 || size < 1 || size > 100) throw ApiException.badRequest("Use page >= 0 e size entre 1 e 100.");
        return repository.findAll(PageRequest.of(page, size, Sort.by("createdAt").ascending().and(Sort.by("id")))).map(ProfessionalView::from);
    }
    @Transactional(readOnly=true)
    public ProfessionalView get(UUID id) { return ProfessionalView.from(find(id)); }
    private Professional find(UUID id) { return repository.findById(id).orElseThrow(() -> ApiException.notFound("Registro não encontrado.")); }
    public ProfessionalView create(ProfessionalRequest request) { return save(new Professional(), request); }
    public ProfessionalView update(UUID id, ProfessionalRequest request) { return save(find(id), request); }
    private ProfessionalView save(Professional entity, ProfessionalRequest request) {
        rules.validate(entity, request);
        entity.userId = request.userId();
        entity.licenseNumber = request.licenseNumber();
        entity.specialty = request.specialty();
        entity.bio = request.bio();
        entity.phone = request.phone();
        return ProfessionalView.from(repository.saveAndFlush(entity));
    }
    public void delete(UUID id) { var entity = find(id); rules.beforeDelete(entity); repository.delete(entity); repository.flush(); }
}
