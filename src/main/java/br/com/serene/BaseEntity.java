package br.com.serene;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
@MappedSuperclass
public abstract class BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID) public UUID id;
    @Column(nullable=false, updatable=false) public OffsetDateTime createdAt;
    @Column(nullable=false) public OffsetDateTime updatedAt;
    @PrePersist void created() { createdAt = OffsetDateTime.now(ZoneOffset.UTC); updatedAt = createdAt; }
    @PreUpdate void updated() { updatedAt = OffsetDateTime.now(ZoneOffset.UTC); }
}
