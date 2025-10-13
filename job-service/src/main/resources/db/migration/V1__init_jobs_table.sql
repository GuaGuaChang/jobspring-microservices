CREATE TABLE jobs
(
    id              BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    company_id      BIGINT UNSIGNED NOT NULL,
    title           VARCHAR(255) NOT NULL,
    location        VARCHAR(255) NULL,
    employment_type TINYINT NULL COMMENT '1=全职 2=实习 3=合同工…',
    salary_min      DECIMAL(10, 2) NULL,
    salary_max      DECIMAL(10, 2) NULL,
    description     LONGTEXT NULL,
    status          TINYINT      NOT NULL DEFAULT 0 COMMENT '0=有效 1=无效',
    posted_at       DATETIME NULL
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci;

CREATE INDEX IDX_jobs_company ON jobs (company_id);
CREATE INDEX IDX_jobs_status_posted ON jobs (posted_at, status);