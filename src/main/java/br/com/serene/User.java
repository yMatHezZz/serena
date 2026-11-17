package br.com.serene;

import jakarta.persistence.*;
import java.util.*;
import java.time.*;
@Entity @Table(name="users")
public class User extends BaseEntity {
    @Column(name="name", columnDefinition="varchar(100)") public String name;
    @Column(name="email", columnDefinition="varchar(100)") public String email;
    @Column(name="status", columnDefinition="varchar(20)") public String status;
    @Column(nullable=false) public String passwordHash;
}
