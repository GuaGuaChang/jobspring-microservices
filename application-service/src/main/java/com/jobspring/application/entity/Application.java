package com.jobspring.application.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "applications")
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "job_id", nullable = false)
    private Long jobId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "profile_id")
    private Long profileId;

    @Column(nullable = false)
    private Integer status = 0;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    private LocalDateTime appliedAt;

    @Column(columnDefinition = "TEXT")
    private String resumeProfile;

    @Lob
    @Column(name = "resume_url", columnDefinition = "LONGTEXT")
    private String resumeUrl;

    @Column(name = "resume_file_id")
    private String resumeFileId;
}

