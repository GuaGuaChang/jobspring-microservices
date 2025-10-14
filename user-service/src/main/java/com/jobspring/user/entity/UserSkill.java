package com.jobspring.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "user_skills",
        uniqueConstraints = @UniqueConstraint(name = "UK_user_skills", columnNames = {"user_id", "skill_id"}))
public class UserSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId; // 不建关系，避免跨表问题

    @Column(name = "skill_id", nullable = false)
    private Long skillId; // 引用 job-service 的技能ID

    @Column(nullable = false)
    private Integer level; // 1~5

    @Column(precision = 3, scale = 1)
    private BigDecimal years; // 经验年限
}
