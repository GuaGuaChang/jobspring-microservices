CREATE TABLE IF NOT EXISTS skills
(
    id
    BIGINT
    UNSIGNED
    AUTO_INCREMENT
    PRIMARY
    KEY,
    name
    VARCHAR
(
    128
) NOT NULL,
    category VARCHAR
(
    64
) NULL,
    CONSTRAINT UK_skills_name UNIQUE
(
    name
)
    ) ENGINE=InnoDB
    DEFAULT CHARSET=utf8mb4
    COLLATE =utf8mb4_0900_ai_ci;


CREATE TABLE IF NOT EXISTS job_skills
(
    id
    BIGINT
    UNSIGNED
    AUTO_INCREMENT
    PRIMARY
    KEY,
    job_id
    BIGINT
    UNSIGNED
    NOT
    NULL,
    skill_id
    BIGINT
    UNSIGNED
    NOT
    NULL,
    required
    TINYINT
(
    1
) NOT NULL DEFAULT 0,
    weight TINYINT NULL COMMENT '0-10',
    CONSTRAINT UK_job_skills UNIQUE
(
    job_id,
    skill_id
),
    CONSTRAINT FK_job_skills_job
    FOREIGN KEY
(
    job_id
) REFERENCES jobs
(
    id
)
    ON UPDATE CASCADE
    ON DELETE CASCADE,
    CONSTRAINT FK_job_skills_skill
    FOREIGN KEY
(
    skill_id
) REFERENCES skills
(
    id
)
    ON UPDATE CASCADE
    ON DELETE CASCADE
    ) ENGINE=InnoDB
    DEFAULT CHARSET=utf8mb4
    COLLATE =utf8mb4_0900_ai_ci;

CREATE INDEX IDX_job_skills_job ON job_skills (job_id);
CREATE INDEX IDX_job_skills_skill ON job_skills (skill_id);