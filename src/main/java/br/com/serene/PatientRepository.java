package br.com.serene;

import java.util.*;
import java.time.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
public interface PatientRepository extends JpaRepository<Patient, UUID> {
    Optional<Patient> findByUserId(UUID userId);
}
