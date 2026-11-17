package br.com.serene;

import java.util.*;
import java.time.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.*;
@Service
@Transactional
public class ActivityService {
    private final ActivityRepository repository;
    private final SchedulingRules rules;
    public ActivityService(ActivityRepository repository, SchedulingRules rules) {
        this.repository = repository; this.rules = rules;
    }
    @Transactional(readOnly=true)
    public Page<ActivityView> list(int page, int size) {
        if (page < 0 || size < 1 || size > 100) throw ApiException.badRequest("Use page >= 0 e size entre 1 e 100.");
        return repository.findAll(PageRequest.of(page, size, Sort.by("createdAt").ascending().and(Sort.by("id")))).map(ActivityView::from);
    }
    @Transactional(readOnly=true)
    public ActivityView get(UUID id) { return ActivityView.from(find(id)); }
    private Activity find(UUID id) { return repository.findById(id).orElseThrow(() -> ApiException.notFound("Registro não encontrado.")); }
    public ActivityView create(ActivityRequest request) { return save(new Activity(), request); }
    public ActivityView update(UUID id, ActivityRequest request) { return save(find(id), request); }
    private ActivityView save(Activity entity, ActivityRequest request) {
        rules.validate(entity, request);
        entity.name = request.name();
        entity.description = request.description();
        entity.activityType = request.activityType();
        entity.configuration = request.configuration();
        entity.active = request.active();
        return ActivityView.from(repository.saveAndFlush(entity));
    }
    public void delete(UUID id) { var entity = find(id); rules.beforeDelete(entity); repository.delete(entity); repository.flush(); }
}
