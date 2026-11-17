package br.com.serene;

import java.util.UUID;
import java.net.URI;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Page;
@RestController @RequestMapping("/api/professionals")
public class ProfessionalController {
    private final ProfessionalService service;
    public ProfessionalController(ProfessionalService service) { this.service = service; }
    @GetMapping public Page<ProfessionalView> list(@RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="20") int size) { return service.list(page, size); }
    @GetMapping("/{id}") public ProfessionalView get(@PathVariable UUID id) { return service.get(id); }
    @PostMapping public ResponseEntity<ProfessionalView> create(@Valid @RequestBody ProfessionalRequest request) {
        var result = service.create(request);
        return ResponseEntity.created(URI.create("/api/professionals/" + result.id())).body(result);
    }
    @PutMapping("/{id}") public ProfessionalView update(@PathVariable UUID id, @Valid @RequestBody ProfessionalRequest request) { return service.update(id, request); }
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable UUID id) { service.delete(id); return ResponseEntity.noContent().build(); }
}
