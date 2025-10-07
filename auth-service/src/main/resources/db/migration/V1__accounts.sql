CREATE TABLE IF NOT EXISTS accounts
(
    id
    BIGINT
    UNSIGNED
    NOT
    NULL
    AUTO_INCREMENT,
    email
    VARCHAR
(
    255
) NOT NULL COMMENT 'Unique email',
    phone VARCHAR
(
    32
) DEFAULT NULL COMMENT 'Phone number',
    password_hash VARCHAR
(
    255
) NOT NULL COMMENT 'Hashed password',
    full_name VARCHAR
(
    128
) NOT NULL COMMENT 'Full name',
    role TINYINT NOT NULL DEFAULT '0' COMMENT '0=Candidate 1=HR 2=Admin',
    is_active TINYINT
(
    1
) NOT NULL DEFAULT '1' COMMENT 'Whether the account is active',
    PRIMARY KEY
(
    id
),
    UNIQUE KEY uk_accounts_email
(
    email
),
    KEY idx_accounts_phone
(
    phone
),
    KEY idx_accounts_role
(
    role
)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE =utf8mb4_0900_ai_ci;