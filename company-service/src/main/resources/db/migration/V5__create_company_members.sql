CREATE TABLE IF NOT EXISTS company_members
(
    id
    BIGINT
    UNSIGNED
    NOT
    NULL
    AUTO_INCREMENT,
    company_id
    BIGINT
    UNSIGNED
    NOT
    NULL,
    user_id
    BIGINT
    UNSIGNED
    NOT
    NULL,
    role
    VARCHAR
(
    64
) NULL COMMENT 'HR / Recruiter / Manager 等',
    PRIMARY KEY
(
    id
),
    CONSTRAINT FK_company_members_company
    FOREIGN KEY
(
    company_id
) REFERENCES companies
(
    id
)
    ON UPDATE CASCADE
    ON DELETE CASCADE,
    KEY IDX_company_members_company
(
    company_id
),
    KEY IDX_company_members_user
(
    user_id
),
    KEY IDX_company_user
(
    company_id,
    user_id
)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE =utf8mb4_0900_ai_ci;
