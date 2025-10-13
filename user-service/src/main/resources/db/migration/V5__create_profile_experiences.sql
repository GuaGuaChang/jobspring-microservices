CREATE TABLE IF NOT EXISTS profile_experiences
(
    id
    BIGINT
    UNSIGNED
    AUTO_INCREMENT
    PRIMARY
    KEY,
    profile_id
    BIGINT
    UNSIGNED
    NOT
    NULL,
    company
    VARCHAR
(
    255
) NULL,
    title VARCHAR
(
    255
) NULL,
    start_date DATE NULL,
    end_date DATE NULL,
    description LONGTEXT NULL,
    achievements LONGTEXT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT FK_profile_experiences_profile
    FOREIGN KEY
(
    profile_id
) REFERENCES profiles
(
    id
)
                                                  ON UPDATE CASCADE
                                                  ON DELETE CASCADE
    ) ENGINE=InnoDB
    DEFAULT CHARSET=utf8mb4
    COLLATE =utf8mb4_0900_ai_ci;

CREATE INDEX IDX_experiences_profile ON profile_experiences (profile_id);