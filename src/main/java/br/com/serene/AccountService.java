package br.com.serene;

import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
@Transactional(readOnly=true)
public class AccountService {
    public record Identity(UUID id, String name, String email, String role) { }
    private final UserRepository users;
    private final JdbcTemplate jdbc;
    private final PasswordEncoder encoder;
    private final String administrator;
    public AccountService(UserRepository users, JdbcTemplate jdbc, PasswordEncoder encoder,
            @Value("${app.admin.username}") String administrator) {
        this.users=users; this.jdbc=jdbc; this.encoder=encoder; this.administrator=administrator;
    }
    @Transactional
    public Identity register(RegistrationRequest request) {
        String email=request.email().trim().toLowerCase(Locale.ROOT);
        if (administrator.equalsIgnoreCase(email) || users.findByEmailIgnoreCase(email).isPresent())
            throw ApiException.conflict("Este e-mail não está disponível para cadastro.");
        if (request.password().getBytes(java.nio.charset.StandardCharsets.UTF_8).length>72)
            throw ApiException.badRequest("A senha deve ter até 72 bytes UTF-8.");
        var user=new User(); user.name=request.name().trim();user.email=email;user.status="ACTIVE";user.passwordHash=encoder.encode(request.password());
        users.saveAndFlush(user);
        jdbc.update("INSERT INTO user_roles(user_id,role_id) SELECT ?,id FROM roles WHERE name=?",user.id,request.role());
        jdbc.update("INSERT INTO registration_consents(user_id,terms_version,privacy_version,consent_version) VALUES (?,?,?,?)",user.id,"demo-2026-11-v1","demo-2026-11-v1","demo-2026-11-v1");
        return new Identity(user.id,user.name,user.email,request.role());
    }
    public Identity identity(Authentication auth) {
        if (auth.getAuthorities().stream().anyMatch(a->a.getAuthority().equals("ROLE_ADMIN")))
            return new Identity(null,auth.getName(),null,"ADMIN");
        var user=users.findByEmailIgnoreCase(auth.getName()).orElseThrow(()->ApiException.notFound("Conta não encontrada."));
        var roles=jdbc.queryForList("SELECT r.name FROM roles r JOIN user_roles ur ON ur.role_id=r.id WHERE ur.user_id=? AND r.name IN ('PATIENT','PROFESSIONAL') ORDER BY r.name",String.class,user.id);
        if (roles.isEmpty()) throw ApiException.notFound("Perfil de acesso não encontrado.");
        return new Identity(user.id,user.name,user.email,roles.get(0));
    }
}
