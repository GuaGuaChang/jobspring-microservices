package com.jobspring.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "profile_educations")
public class ProfileEducation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "profile_id", nullable = false)
    private Long profileId;

    @Column(nullable = false, length = 255)
    private String school;

    private String degree;
    private String major;

    private LocalDate startDate;
    private LocalDate endDate;

    private BigDecimal gpa;

    @Lob
    private String description;
}
