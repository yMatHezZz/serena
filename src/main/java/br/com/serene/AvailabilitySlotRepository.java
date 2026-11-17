package br.com.serene;

import java.util.*;
import java.time.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
public interface AvailabilitySlotRepository extends JpaRepository<AvailabilitySlot, UUID> {
    @Query("select s from AvailabilitySlot s where s.professionalId = :professional and s.available = true and s.startAt <= :start and s.endAt >= :end")
    List<AvailabilitySlot> covering(@Param("professional") UUID professional, @Param("start") OffsetDateTime start, @Param("end") OffsetDateTime end);
}
