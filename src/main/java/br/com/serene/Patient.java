package br.com.serene;

import jakarta.persistence.*;
import java.util.*;
import java.time.*;
@Entity @Table(name="patient_profiles")
public class Patient extends BaseEntity {
    @Column(name="user_id", columnDefinition="uuid") public UUID userId;
    @Column(name="date_of_birth", columnDefinition="date") public LocalDate dateOfBirth;
    @Column(name="gender", columnDefinition="varchar(20)") public String gender;
    @Column(name="phone", columnDefinition="varchar(20)") public String phone;
    @Column(name="address", columnDefinition="text") public String address;
}
