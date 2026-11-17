package br.com.serene;

import jakarta.persistence.*;
import java.util.*;
import java.time.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
@Entity @Table(name="activities")
public class Activity extends BaseEntity {
    @Column(name="name", columnDefinition="varchar(100)") public String name;
    @Column(name="description", columnDefinition="text") public String description;
    @Column(name="activity_type", columnDefinition="varchar(50)") public String activityType;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name="configuration", columnDefinition="jsonb") public Map<String, Object> configuration;
    @Column(name="is_active", columnDefinition="boolean") public Boolean active;
}
