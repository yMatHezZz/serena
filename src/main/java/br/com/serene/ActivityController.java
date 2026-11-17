package br.com.serene;

import java.util.UUID;
import java.net.URI;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Page;
@RestController @RequestMapping("/api/activities")
public class ActivityController {
    private final ActivityService service;
    public ActivityController(ActivityService service) { this.service = service; }
    @GetMapping public Page<ActivityView> list(@RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="20") int size) { return service.list(page, size); }
    @GetMapping("/{id}") public ActivityView get(@PathVariable UUID id) { return service.get(id); }
    @PostMapping public ResponseEntity<ActivityView> create(@Valid @RequestBody ActivityRequest request) {
        var result = service.create(request);
        return ResponseEntity.created(URI.create("/api/activities/" + result.id())).body(result);
    }
    @PutMapping("/{id}") public ActivityView update(@PathVariable UUID id, @Valid @RequestBody ActivityRequest request) { return service.update(id, request); }
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable UUID id) { service.delete(id); return ResponseEntity.noContent().build(); }
}
