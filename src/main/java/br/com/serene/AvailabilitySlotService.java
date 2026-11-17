package br.com.serene;

import java.util.*;
import java.time.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.*;
@Service
@Transactional
public class AvailabilitySlotService {
    private final AvailabilitySlotRepository repository;
    private final SchedulingRules rules;
    public AvailabilitySlotService(AvailabilitySlotRepository repository, SchedulingRules rules) {
        this.repository = repository; this.rules = rules;
    }
    @Transactional(readOnly=true)
    public Page<AvailabilitySlotView> list(int page, int size) {
        if (page < 0 || size < 1 || size > 100) throw ApiException.badRequest("Use page >= 0 e size entre 1 e 100.");
        return repository.findAll(PageRequest.of(page, size, Sort.by("createdAt").ascending().and(Sort.by("id")))).map(AvailabilitySlotView::from);
    }
    @Transactional(readOnly=true)
    public AvailabilitySlotView get(UUID id) { return AvailabilitySlotView.from(find(id)); }
    private AvailabilitySlot find(UUID id) { return repository.findById(id).orElseThrow(() -> ApiException.notFound("Registro não encontrado.")); }
    public AvailabilitySlotView create(AvailabilitySlotRequest request) { return save(new AvailabilitySlot(), request); }
    public AvailabilitySlotView update(UUID id, AvailabilitySlotRequest request) { return save(find(id), request); }
    private AvailabilitySlotView save(AvailabilitySlot entity, AvailabilitySlotRequest request) {
        rules.validate(entity, request);
        entity.professionalId = request.professionalId();
        entity.startAt = request.startAt();
        entity.endAt = request.endAt();
        entity.available = request.available();
        return AvailabilitySlotView.from(repository.saveAndFlush(entity));
    }
    public void delete(UUID id) { var entity = find(id); rules.beforeDelete(entity); repository.delete(entity); repository.flush(); }
}
