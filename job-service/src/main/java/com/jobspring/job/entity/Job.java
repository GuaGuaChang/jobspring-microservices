package com.jobspring.job.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "jobs",
        indexes = {
                @Index(name = "idx_jobs_company", columnList = "company_id"),
                @Index(name = "idx_jobs_status", columnList = "status"),
                @Index(name = "idx_jobs_title", columnList = "title")
        })
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 跨服务不建立实体关联，只保存 company_id
    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(length = 255)
    private String location;

    /** 1 = 全职，2 = 兼职/实习，3 = 远程/合同等（与前端/协议保持一致） */
    @Column(name = "employment_type", nullable = false)
    private Integer employmentType = 1;

    @Column(name = "salary_min", precision = 10, scale = 2)
    private BigDecimal salaryMin;

    @Column(name = "salary_max", precision = 10, scale = 2)
    private BigDecimal salaryMax;

    @Lob
    private String description;

    /** 0 = 有效，1 = 无效/下线 */
    @Column(nullable = false)
    private Integer status = 0;

    @CreationTimestamp
    @Column(name = "posted_at", updatable = false)
    private LocalDateTime postedAt;
}