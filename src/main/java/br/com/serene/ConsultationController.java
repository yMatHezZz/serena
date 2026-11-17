package br.com.serene;

import java.util.UUID;
import java.net.URI;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Page;
@RestController @RequestMapping("/api/consultations")
public class ConsultationController {
    private final ConsultationService service;
    public ConsultationController(ConsultationService service) { this.service = service; }
    @GetMapping public Page<ConsultationView> list(@RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="20") int size) { return service.list(page, size); }
    @GetMapping("/{id}") public ConsultationView get(@PathVariable UUID id) { return service.get(id); }
    @PostMapping public ResponseEntity<ConsultationView> create(@Valid @RequestBody ConsultationRequest request) {
        var result = service.create(request);
        return ResponseEntity.created(URI.create("/api/consultations/" + result.id())).body(result);
    }
    @PutMapping("/{id}") public ConsultationView update(@PathVariable UUID id, @Valid @RequestBody ConsultationRequest request) { return service.update(id, request); }
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable UUID id) { service.delete(id); return ResponseEntity.noContent().build(); }
}
