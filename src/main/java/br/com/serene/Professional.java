package br.com.serene;

import jakarta.persistence.*;
import java.util.*;
import java.time.*;
@Entity @Table(name="professional_profiles")
public class Professional extends BaseEntity {
    @Column(name="user_id", columnDefinition="uuid") public UUID userId;
    @Column(name="license_number", columnDefinition="varchar(50)") public String licenseNumber;
    @Column(name="specialty", columnDefinition="varchar(100)") public String specialty;
    @Column(name="bio", columnDefinition="text") public String bio;
    @Column(name="phone", columnDefinition="varchar(20)") public String phone;
}
