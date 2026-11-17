package br.com.serene;

import org.springframework.context.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.http.HttpMethod;
@Configuration
public class SecurityConfig {
    @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
    @Bean UserDetailsService administrators(@Value("${app.admin.username}") String username,
            @Value("${app.admin.password}") String password, PasswordEncoder encoder,
            UserRepository users, org.springframework.jdbc.core.JdbcTemplate jdbc) {
        if (username.isBlank() || password.length() < 12 || password.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72)
            throw new IllegalArgumentException("Configure ADMIN_USERNAME e ADMIN_PASSWORD (12 caracteres ou mais, até 72 bytes).");
        String administratorHash=encoder.encode(password);
        return login -> {
            if (username.equalsIgnoreCase(login)) return org.springframework.security.core.userdetails.User.withUsername(username)
                .password(administratorHash).roles("ADMIN").build();
            var user = users.findByEmailIgnoreCase(login).orElseThrow(() -> new UsernameNotFoundException("Conta não encontrada."));
            var roles = jdbc.queryForList("SELECT r.name FROM roles r JOIN user_roles ur ON ur.role_id=r.id WHERE ur.user_id=? AND r.name IN ('PATIENT','PROFESSIONAL')", String.class, user.id);
            if (roles.isEmpty()) throw new UsernameNotFoundException("Conta sem acesso público.");
            return org.springframework.security.core.userdetails.User.withUsername(user.email).password(user.passwordHash)
                .roles(roles.toArray(String[]::new)).disabled(!"ACTIVE".equals(user.status)).build();
        };
    }
    @Bean SecurityFilterChain security(HttpSecurity http) throws Exception {
        // Public shell; all data stays behind administrative authentication.
        return http.csrf(csrf -> csrf.disable()).cors(cors -> {})
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(a -> a
                .requestMatchers(HttpMethod.GET, "/", "/index.html", "/assets/**", "/favicon.svg", "/api", "/api/").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/register").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/auth/me").authenticated()
                .requestMatchers("/api/account/**", "/api/account").hasAnyRole("PATIENT", "PROFESSIONAL")
                .anyRequest().hasRole("ADMIN"))
            .exceptionHandling(e -> e.accessDeniedHandler((request, response, ex) -> {
                response.setStatus(403); response.setContentType("application/json");
                response.getWriter().write("{\"status\":403,\"message\":\"Sua conta não tem acesso a este recurso.\",\"errors\":{}}");
            }))
            .httpBasic(b -> b.authenticationEntryPoint((request, response, ex) -> {
                response.setStatus(401);
                if (!"XMLHttpRequest".equals(request.getHeader("X-Requested-With")))
                    response.setHeader("WWW-Authenticate", "Basic realm=\"serene\"");
                response.setContentType("application/json"); response.getWriter().write("{\"status\":401,\"message\":\"Autenticação administrativa necessária.\",\"errors\":{}}");
            }))
            .addFilterBefore(new OriginGuardFilter(), org.springframework.security.web.authentication.www.BasicAuthenticationFilter.class)
            .build();
    }
}
