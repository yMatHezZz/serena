package br.com.serene;

import java.util.*;
import java.time.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;
@Service
@Transactional
public class UserService {
    private final UserRepository repository;
    private final SchedulingRules rules;
    private final PasswordEncoder encoder;
    public UserService(UserRepository repository, SchedulingRules rules, PasswordEncoder encoder) {
        this.repository = repository; this.rules = rules; this.encoder = encoder;
    }
    @Transactional(readOnly=true)
    public Page<UserView> list(int page, int size) {
        if (page < 0 || size < 1 || size > 100) throw ApiException.badRequest("Use page >= 0 e size entre 1 e 100.");
        return repository.findAll(PageRequest.of(page, size, Sort.by("createdAt").ascending().and(Sort.by("id")))).map(UserView::from);
    }
    @Transactional(readOnly=true)
    public UserView get(UUID id) { return UserView.from(find(id)); }
    private User find(UUID id) { return repository.findById(id).orElseThrow(() -> ApiException.notFound("Registro não encontrado.")); }
    public UserView create(UserRequest request) { return save(new User(), request); }
    public UserView update(UUID id, UserRequest request) { return save(find(id), request); }
    private UserView save(User entity, UserRequest request) {
        rules.validate(entity, request);
        entity.name = request.name();
        entity.email = request.email() .trim().toLowerCase(Locale.ROOT);
        entity.status = request.status();
        if (request.password() != null) {
            if (request.password().getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72)
                throw ApiException.badRequest("A senha deve ter até 72 bytes UTF-8.");
            entity.passwordHash = encoder.encode(request.password());
        } else if (entity.id == null) throw ApiException.badRequest("Senha obrigatória no cadastro.");
        return UserView.from(repository.saveAndFlush(entity));
    }
    public void delete(UUID id) { var entity = find(id); rules.beforeDelete(entity); repository.delete(entity); repository.flush(); }
}
