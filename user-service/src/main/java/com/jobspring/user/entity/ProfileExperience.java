package com.jobspring.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "profile_experiences")
public class ProfileExperience {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "profile_id", nullable = false)
    private Long profileId;

    private String company;
    private String title;

    private LocalDate startDate;
    private LocalDate endDate;

    @Lob
    private String description;
    @Lob
    private String achievements;
}
