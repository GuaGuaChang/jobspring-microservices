CREATE TABLE IF NOT EXISTS user_skills
(
    id
    BIGINT
    UNSIGNED
    AUTO_INCREMENT
    PRIMARY
    KEY,
    user_id
    BIGINT
    UNSIGNED
    NOT
    NULL,
    skill_id
    BIGINT
    UNSIGNED
    NOT
    NULL
    COMMENT
    '引用 job-service.skills.id',
    level
    INT
    NULL
    COMMENT
    '1~5 等级',
    years
    DECIMAL
(
    3,
    1
) NULL COMMENT '经验年限',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT UK_user_skills UNIQUE
(
    user_id,
    skill_id
)
    ) ENGINE=InnoDB
    DEFAULT CHARSET=utf8mb4
    COLLATE =utf8mb4_0900_ai_ci;

CREATE INDEX IDX_user_skills_user ON user_skills (user_id);
CREATE INDEX IDX_user_skills_skill ON user_skills (skill_id);