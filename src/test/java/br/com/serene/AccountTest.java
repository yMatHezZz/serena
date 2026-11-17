package br.com.serene;

import java.util.*;
import com.fasterxml.jackson.databind.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test")
class AccountTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired org.springframework.jdbc.core.JdbcTemplate jdbc;
    String email() { return UUID.randomUUID()+"@example.test"; }
    Map<String,Object> registration(String email, String role) {
        return new HashMap<>(Map.of("name","Conta fictícia","email",email,"password","Synthetic123!","role",role,
            "termsAccepted",true,"privacyAccepted",true,"consentAccepted",true));
    }
    void register(String email, String role) throws Exception {
        mvc.perform(post("/api/auth/register").contentType("application/json").content(json.writeValueAsString(registration(email,role))))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.role").value(role)).andExpect(jsonPath("$.passwordHash").doesNotExist());
    }
    @Test void publicRegistrationCreatesPatientWhoCannotUseAdminApi() throws Exception {
        String email=email();register(email,"PATIENT");
        mvc.perform(get("/api/auth/me").with(httpBasic(email,"Synthetic123!"))).andExpect(status().isOk()).andExpect(jsonPath("$.role").value("PATIENT"));
        mvc.perform(get("/api/users").with(httpBasic(email,"Synthetic123!"))).andExpect(status().isForbidden());
        mvc.perform(get("/api/account").with(httpBasic(email,"Synthetic123!"))).andExpect(status().isOk()).andExpect(jsonPath("$.profile").isEmpty());
        mvc.perform(put("/api/account/patient-profile").with(httpBasic(email,"Synthetic123!")).contentType("application/json").content("{\"dateOfBirth\":\"1990-01-01\",\"phone\":\"11999990000\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.phone").value("11999990000"));
    }
    @Test void acceptsOnlyKnownRolesAndRequiredAcceptances() throws Exception {
        for (String role : List.of("ADMIN","UNKNOWN")) {
            mvc.perform(post("/api/auth/register").contentType("application/json").content(json.writeValueAsString(registration(email(),role)))).andExpect(status().isBadRequest());
        }
        var body=registration(email(),"PATIENT");body.put("privacyAccepted",false);
        mvc.perform(post("/api/auth/register").contentType("application/json").content(json.writeValueAsString(body))).andExpect(status().isBadRequest());
        mvc.perform(get("/api/account")).andExpect(status().isUnauthorized());
    }
    @Test void duplicateEmailAndCrossRoleAccessAreBlocked() throws Exception {
        String email=email();register(email,"PROFESSIONAL");
        mvc.perform(post("/api/auth/register").contentType("application/json").content(json.writeValueAsString(registration(email,"PATIENT")))).andExpect(status().isConflict());
        mvc.perform(put("/api/account/patient-profile").with(httpBasic(email,"Synthetic123!")).contentType("application/json").content("{\"dateOfBirth\":\"1990-01-01\"}"))
            .andExpect(status().isForbidden());
        mvc.perform(put("/api/account/professional-profile").with(httpBasic(email,"Synthetic123!")).contentType("application/json")
            .content(json.writeValueAsString(Map.of("licenseNumber","QA-"+UUID.randomUUID(),"specialty","Psicologia fictícia"))))
            .andExpect(status().isOk());
    }
    @Test void profilesAreIsolatedAndCannotBeAssignedByClient() throws Exception {
        String a=email(),b=email();register(a,"PATIENT");register(b,"PATIENT");
        String own=mvc.perform(put("/api/account/patient-profile").with(httpBasic(a,"Synthetic123!")).contentType("application/json").content("{\"dateOfBirth\":\"1990-01-01\",\"phone\":\"1111\"}"))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        mvc.perform(get("/api/account").with(httpBasic(b,"Synthetic123!"))).andExpect(status().isOk()).andExpect(jsonPath("$.profile").isEmpty());
        mvc.perform(get("/api/patients/"+json.readTree(own).get("id").asText()).with(httpBasic(b,"Synthetic123!"))).andExpect(status().isForbidden());
        mvc.perform(put("/api/account/patient-profile").with(httpBasic(b,"Synthetic123!")).contentType("application/json").content("{\"dateOfBirth\":\"1990-01-01\",\"userId\":\"00000000-0000-0000-0000-000000000000\"}"))
            .andExpect(status().isBadRequest());
        mvc.perform(get("/api/account").with(httpBasic(a,"Synthetic123!"))).andExpect(jsonPath("$.profile.phone").value("1111"));
    }
    @Test void administratorCanAuthenticateOnConsecutiveRequests() throws Exception {
        for (int i=0;i<3;i++) {
            mvc.perform(get("/api/auth/me").with(httpBasic("test-admin","synthetic-test-password-only")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.role").value("ADMIN"));
        }
    }
    @Test void inactiveAccountCannotAuthenticate() throws Exception {
        String email=email();register(email,"PATIENT");
        jdbc.update("UPDATE users SET status='INACTIVE' WHERE email=?",email);
        mvc.perform(get("/api/auth/me").with(httpBasic(email,"Synthetic123!"))).andExpect(status().isUnauthorized());
    }
    @Test void consultationReadIsScopedToAuthenticatedPatientOrProfessional() throws Exception {
        String a=email(),b=email(),pro=email();register(a,"PATIENT");register(b,"PATIENT");register(pro,"PROFESSIONAL");
        UUID[] patientIds=new UUID[2];int index=0;
        for (String email:List.of(a,b)) {
            String response=mvc.perform(put("/api/account/patient-profile").with(httpBasic(email,"Synthetic123!")).contentType("application/json").content("{\"dateOfBirth\":\"1990-01-01\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
            patientIds[index++]=UUID.fromString(json.readTree(response).get("id").asText());
        }
        String professional=mvc.perform(put("/api/account/professional-profile").with(httpBasic(pro,"Synthetic123!")).contentType("application/json")
            .content(json.writeValueAsString(Map.of("licenseNumber","QA-"+UUID.randomUUID(),"specialty","Teste"))))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        UUID proId=UUID.fromString(json.readTree(professional).get("id").asText());
        UUID first=UUID.randomUUID(),second=UUID.randomUUID();
        jdbc.update("INSERT INTO consultations(id,patient_id,professional_id,scheduled_start_at,scheduled_end_at,status,created_at,updated_at) VALUES (?,?,?,'2099-06-01T10:00Z','2099-06-01T11:00Z','SCHEDULED',now(),now()),(?,?,?,'2099-06-01T11:00Z','2099-06-01T12:00Z','SCHEDULED',now(),now())",first,patientIds[0],proId,second,patientIds[1],proId);
        mvc.perform(get("/api/account").with(httpBasic(a,"Synthetic123!"))).andExpect(jsonPath("$.consultations.length()").value(1)).andExpect(jsonPath("$.consultations[0].id").value(first.toString()));
        mvc.perform(get("/api/account").with(httpBasic(b,"Synthetic123!"))).andExpect(jsonPath("$.consultations.length()").value(1)).andExpect(jsonPath("$.consultations[0].id").value(second.toString()));
        mvc.perform(get("/api/account").with(httpBasic(pro,"Synthetic123!"))).andExpect(jsonPath("$.consultations.length()").value(2));
        jdbc.update("DELETE FROM consultations WHERE id IN (?,?)",first,second);
    }
}
