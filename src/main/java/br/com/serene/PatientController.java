package br.com.serene;

import java.util.UUID;
import java.net.URI;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Page;
@RestController @RequestMapping("/api/patients")
public class PatientController {
    private final PatientService service;
    public PatientController(PatientService service) { this.service = service; }
    @GetMapping public Page<PatientView> list(@RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="20") int size) { return service.list(page, size); }
    @GetMapping("/{id}") public PatientView get(@PathVariable UUID id) { return service.get(id); }
    @PostMapping public ResponseEntity<PatientView> create(@Valid @RequestBody PatientRequest request) {
        var result = service.create(request);
        return ResponseEntity.created(URI.create("/api/patients/" + result.id())).body(result);
    }
    @PutMapping("/{id}") public PatientView update(@PathVariable UUID id, @Valid @RequestBody PatientRequest request) { return service.update(id, request); }
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable UUID id) { service.delete(id); return ResponseEntity.noContent().build(); }
}
