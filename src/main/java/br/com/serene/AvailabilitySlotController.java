package br.com.serene;

import java.util.UUID;
import java.net.URI;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Page;
@RestController @RequestMapping("/api/availability-slots")
public class AvailabilitySlotController {
    private final AvailabilitySlotService service;
    public AvailabilitySlotController(AvailabilitySlotService service) { this.service = service; }
    @GetMapping public Page<AvailabilitySlotView> list(@RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="20") int size) { return service.list(page, size); }
    @GetMapping("/{id}") public AvailabilitySlotView get(@PathVariable UUID id) { return service.get(id); }
    @PostMapping public ResponseEntity<AvailabilitySlotView> create(@Valid @RequestBody AvailabilitySlotRequest request) {
        var result = service.create(request);
        return ResponseEntity.created(URI.create("/api/availability-slots/" + result.id())).body(result);
    }
    @PutMapping("/{id}") public AvailabilitySlotView update(@PathVariable UUID id, @Valid @RequestBody AvailabilitySlotRequest request) { return service.update(id, request); }
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable UUID id) { service.delete(id); return ResponseEntity.noContent().build(); }
}
