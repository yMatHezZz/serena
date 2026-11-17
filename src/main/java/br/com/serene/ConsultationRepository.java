package br.com.serene;

import java.util.*;
import java.time.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
public interface ConsultationRepository extends JpaRepository<Consultation, UUID> {
    java.util.List<Consultation> findByPatientId(UUID patientId, org.springframework.data.domain.Pageable page);
    java.util.List<Consultation> findByProfessionalId(UUID professionalId, org.springframework.data.domain.Pageable page);
    @Query("select c from Consultation c where c.professionalId = :professional and c.status = 'SCHEDULED'")
    List<Consultation> scheduledFor(@Param("professional") UUID professional);
}
