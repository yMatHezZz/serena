package br.com.serene;

import java.util.UUID;
import java.net.URI;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Page;
@RestController @RequestMapping("/api/users")
public class UserController {
    private final UserService service;
    public UserController(UserService service) { this.service = service; }
    @GetMapping public Page<UserView> list(@RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="20") int size) { return service.list(page, size); }
    @GetMapping("/{id}") public UserView get(@PathVariable UUID id) { return service.get(id); }
    @PostMapping public ResponseEntity<UserView> create(@Valid @RequestBody UserRequest request) {
        var result = service.create(request);
        return ResponseEntity.created(URI.create("/api/users/" + result.id())).body(result);
    }
    @PutMapping("/{id}") public UserView update(@PathVariable UUID id, @Valid @RequestBody UserRequest request) { return service.update(id, request); }
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable UUID id) { service.delete(id); return ResponseEntity.noContent().build(); }
}
