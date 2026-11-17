package br.com.serene;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = SereneApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    JsonNode create(String path, Object body) throws Exception {
        return json.readTree(mvc.perform(post("/api/" + path).with(httpBasic("test-admin", "synthetic-test-password-only"))
            .contentType("application/json").content(json.writeValueAsString(body)))
            .andExpect(status().isCreated()).andExpect(header().exists("Location")).andReturn().getResponse().getContentAsString());
    }
    String user(String email) throws Exception {
        JsonNode u = create("users", Map.of("name", "Pessoa fictícia", "email", java.util.UUID.randomUUID()+email, "password", "Synthetic123!", "status", "ACTIVE"));
        assertThat(u.has("password")).isFalse(); assertThat(u.has("passwordHash")).isFalse();
        return u.get("id").asText();
    }
    void putOk(String path, Object body) throws Exception {
        mvc.perform(put("/api/" + path).with(httpBasic("test-admin", "synthetic-test-password-only"))
            .contentType("application/json").content(json.writeValueAsString(body))).andExpect(status().isOk());
    }
    void deleteOk(String path) throws Exception {
        mvc.perform(delete("/api/" + path).with(httpBasic("test-admin", "synthetic-test-password-only"))).andExpect(status().isNoContent());
        mvc.perform(get("/api/" + path).with(httpBasic("test-admin", "synthetic-test-password-only"))).andExpect(status().isNotFound());
    }
    @Test void requiresAdminAuthentication() throws Exception {
        mvc.perform(get("/api/users")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/users").with(httpBasic("invalid", "invalid"))).andExpect(status().isUnauthorized());
    }
    @Test void frontendIsPublicButApiStillRequiresAuthentication() throws Exception {
        mvc.perform(get("/")).andExpect(status().isOk()).andExpect(forwardedUrl("index.html"));
        mvc.perform(get("/index.html")).andExpect(status().isOk()).andExpect(content().contentTypeCompatibleWith("text/html"));
        mvc.perform(get("/assets/app.js")).andExpect(status().isOk());
        mvc.perform(get("/api")).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/"));
        mvc.perform(get("/api/patients")).andExpect(status().isUnauthorized());
    }
    @Test void sameOriginFrontendCanWriteButOtherOriginsCannot() throws Exception {
        String payload = json.writeValueAsString(Map.of("name","Web sintético","email",java.util.UUID.randomUUID()+"@example.test","password","Synthetic123!","status","ACTIVE"));
        String result = mvc.perform(post("/api/users").header("Origin","http://localhost").header("X-Serene-Client","admin-web")
            .with(httpBasic("test-admin","synthetic-test-password-only")).contentType("application/json").content(payload))
            .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        deleteOk("users/"+json.readTree(result).get("id").asText());
        for (String origin : new String[]{"http://localhost:9999","https://evil.example","null"}) {
            mvc.perform(post("/api/users").header("Origin",origin).header("X-Serene-Client","admin-web")
                .with(httpBasic("test-admin","synthetic-test-password-only")).contentType("application/json").content(payload)).andExpect(status().isForbidden());
        }
        mvc.perform(post("/api/users").header("Origin","http://localhost").with(httpBasic("test-admin","synthetic-test-password-only"))
            .contentType("application/json").content(payload)).andExpect(status().isForbidden());
    }
    @Test void rejectsInvalidInputAndReturns404() throws Exception {
        mvc.perform(post("/api/users").with(httpBasic("test-admin", "synthetic-test-password-only"))
            .contentType("application/json").content("{\"name\":\"\",\"email\":\"bad\",\"password\":\"short\",\"status\":\"ACTIVE\"}"))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors.email").exists());
        mvc.perform(get("/api/users/00000000-0000-0000-0000-000000000000").with(httpBasic("test-admin", "synthetic-test-password-only"))).andExpect(status().isNotFound());
        mvc.perform(get("/api/users/not-uuid").with(httpBasic("test-admin", "synthetic-test-password-only"))).andExpect(status().isBadRequest());
    }
    @Test void fullCrudAndSchedulingRules() throws Exception {
        String patientUser = user("patient@example.test"), professionalUser = user("professional@example.test");
        var patientBody = Map.of("userId", patientUser, "dateOfBirth", "1990-01-01", "gender", "NAO_INFORMADO", "phone", "11999999999", "address", "Endereço fictício");
        String patient = create("patients", patientBody).get("id").asText();
        var professionalBody = Map.of("userId", professionalUser, "licenseNumber", "TEST-"+java.util.UUID.randomUUID(), "specialty", "Psicologia", "bio", "Perfil fictício", "phone", "11999999998");
        String professional = create("professionals", professionalBody).get("id").asText();
        var slotBody = Map.of("professionalId", professional, "startAt", "2099-01-01T12:00:00Z", "endAt", "2099-01-01T16:00:00Z", "available", true);
        String slot = create("availability-slots", slotBody).get("id").asText();
        var booking = Map.of("patientId", patient, "professionalId", professional, "scheduledStartAt", "2099-01-01T12:00:00Z", "scheduledEndAt", "2099-01-01T13:00:00Z", "status", "SCHEDULED");
        String consultation = create("consultations", booking).get("id").asText();
        mvc.perform(post("/api/consultations").with(httpBasic("test-admin", "synthetic-test-password-only")).contentType("application/json").content(json.writeValueAsString(booking)))
            .andExpect(status().isConflict());
        mvc.perform(delete("/api/availability-slots/" + slot).with(httpBasic("test-admin", "synthetic-test-password-only"))).andExpect(status().isConflict());
        var activityBody = Map.of("name", "Respiração", "description", "Demonstração", "activityType", "BREATHING", "configuration", Map.of("seconds", 30), "active", true);
        String activity = create("activities", activityBody).get("id").asText();
        for (String path : new String[]{"users/"+patientUser, "patients/"+patient, "professionals/"+professional, "availability-slots/"+slot, "consultations/"+consultation, "activities/"+activity}) {
            mvc.perform(get("/api/"+path).with(httpBasic("test-admin", "synthetic-test-password-only"))).andExpect(status().isOk());
            mvc.perform(get("/api/"+path.substring(0,path.indexOf('/'))).with(httpBasic("test-admin", "synthetic-test-password-only"))).andExpect(status().isOk()).andExpect(jsonPath("$.content").isArray());
        }
        putOk("patients/"+patient, patientBody); putOk("professionals/"+professional, professionalBody);
        putOk("availability-slots/"+slot, slotBody); putOk("consultations/"+consultation, booking); putOk("activities/"+activity, activityBody);
        putOk("users/"+patientUser, Map.of("name","Nome alterado", "email",java.util.UUID.randomUUID()+"@example.test", "status","ACTIVE"));
        deleteOk("consultations/"+consultation); deleteOk("availability-slots/"+slot); deleteOk("patients/"+patient); deleteOk("professionals/"+professional);
        deleteOk("users/"+patientUser); deleteOk("users/"+professionalUser); deleteOk("activities/"+activity);
    }

    @Test void cancellationReleasesSlotAndRejectsInvalidTransitions() throws Exception {
        String pu = user("@example.test"), du = user("@example.test");
        String patient = create("patients", Map.of("userId",pu,"dateOfBirth","1990-01-01")).get("id").asText();
        String professional = create("professionals", Map.of("userId",du,"licenseNumber",java.util.UUID.randomUUID().toString(),"specialty","Teste")).get("id").asText();
        var outside = new java.util.HashMap<String,Object>(Map.of("patientId",patient,"professionalId",professional,"scheduledStartAt","2099-02-01T10:00:00Z","scheduledEndAt","2099-02-01T11:00:00Z","status","SCHEDULED"));
        mvc.perform(post("/api/consultations").with(httpBasic("test-admin","synthetic-test-password-only")).contentType("application/json").content(json.writeValueAsString(outside))).andExpect(status().isConflict());
        var slot = Map.of("professionalId",professional,"startAt","2099-02-01T10:00:00Z","endAt","2099-02-01T12:00:00Z","available",true);
        String slotId = create("availability-slots",slot).get("id").asText();
        String id = create("consultations",outside).get("id").asText();
        outside.put("status","COMPLETED");
        mvc.perform(put("/api/consultations/"+id).with(httpBasic("test-admin","synthetic-test-password-only")).contentType("application/json").content(json.writeValueAsString(outside))).andExpect(status().isConflict());
        outside.put("status","CANCELLED"); putOk("consultations/"+id,outside);
        outside.put("status","SCHEDULED");
        String replacement = create("consultations",outside).get("id").asText();
        mvc.perform(put("/api/consultations/"+id).with(httpBasic("test-admin","synthetic-test-password-only")).contentType("application/json").content(json.writeValueAsString(outside))).andExpect(status().isConflict());
        outside.put("scheduledEndAt","2099-02-01T09:00:00Z");
        mvc.perform(post("/api/consultations").with(httpBasic("test-admin","synthetic-test-password-only")).contentType("application/json").content(json.writeValueAsString(outside))).andExpect(status().isBadRequest());
        deleteOk("consultations/"+replacement); deleteOk("consultations/"+id); deleteOk("availability-slots/"+slotId);
        deleteOk("patients/"+patient); deleteOk("professionals/"+professional); deleteOk("users/"+pu); deleteOk("users/"+du);
    }

    @Test void duplicateEmailAndLinkedDeletionAreConflicts() throws Exception {
        String email = java.util.UUID.randomUUID()+"@example.test";
        var body = Map.of("name","Teste","email",email,"password","Synthetic123!","status","ACTIVE");
        String id = create("users",body).get("id").asText();
        mvc.perform(post("/api/users").with(httpBasic("test-admin","synthetic-test-password-only")).contentType("application/json").content(json.writeValueAsString(body))).andExpect(status().isConflict());
        String patient = create("patients",Map.of("userId",id,"dateOfBirth","2000-01-01")).get("id").asText();
        mvc.perform(delete("/api/users/"+id).with(httpBasic("test-admin","synthetic-test-password-only"))).andExpect(status().isConflict());
        deleteOk("patients/"+patient); deleteOk("users/"+id);
    }

    @Test void browserWriteIsForbiddenAndPaginationIsBounded() throws Exception {
        mvc.perform(post("/api/users").header("Origin","https://example.test").with(httpBasic("test-admin","synthetic-test-password-only")).contentType("application/json").content("{}"))
            .andExpect(status().isForbidden());
        mvc.perform(get("/api/users?size=101").with(httpBasic("test-admin","synthetic-test-password-only"))).andExpect(status().isBadRequest());
        mvc.perform(get("/api/users?page=-1").with(httpBasic("test-admin","synthetic-test-password-only"))).andExpect(status().isBadRequest());
    }

    @Test void concurrentBookingsHaveExactlyOneWinner() throws Exception {
        String pu = user("@example.test"), du = user("@example.test");
        String patient = create("patients",Map.of("userId",pu,"dateOfBirth","1990-01-01")).get("id").asText();
        String professional = create("professionals",Map.of("userId",du,"licenseNumber",java.util.UUID.randomUUID().toString(),"specialty","Teste")).get("id").asText();
        String slot = create("availability-slots",Map.of("professionalId",professional,"startAt","2099-03-01T10:00:00Z","endAt","2099-03-01T12:00:00Z","available",true)).get("id").asText();
        String payload = json.writeValueAsString(Map.of("patientId",patient,"professionalId",professional,"scheduledStartAt","2099-03-01T10:00:00Z","scheduledEndAt","2099-03-01T11:00:00Z","status","SCHEDULED"));
        var gate = new java.util.concurrent.CountDownLatch(1);
        var executor = java.util.concurrent.Executors.newFixedThreadPool(2);
        try {
            java.util.concurrent.Callable<org.springframework.test.web.servlet.MvcResult> task = () -> {
                gate.await(); return mvc.perform(post("/api/consultations").with(httpBasic("test-admin","synthetic-test-password-only")).contentType("application/json").content(payload)).andReturn();
            };
            var first = executor.submit(task); var second = executor.submit(task); gate.countDown();
            var a = first.get(20, java.util.concurrent.TimeUnit.SECONDS); var b = second.get(20, java.util.concurrent.TimeUnit.SECONDS);
            assertThat(java.util.List.of(a.getResponse().getStatus(),b.getResponse().getStatus())).containsExactlyInAnyOrder(201,409);
            var winner = a.getResponse().getStatus()==201 ? a : b;
            deleteOk("consultations/"+json.readTree(winner.getResponse().getContentAsString()).get("id").asText());
        } finally { executor.shutdownNow(); }
        deleteOk("availability-slots/"+slot); deleteOk("patients/"+patient); deleteOk("professionals/"+professional); deleteOk("users/"+pu); deleteOk("users/"+du);
    }
}
