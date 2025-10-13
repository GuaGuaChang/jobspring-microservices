CREATE TABLE IF NOT EXISTS profile_educations
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
    school
    VARCHAR
(
    255
) NULL,
    degree VARCHAR
(
    128
) NULL,
    major VARCHAR
(
    128
) NULL,
    start_date DATE NULL,
    end_date DATE NULL,
    gpa DECIMAL
(
    3,
    2
) NULL,
    description LONGTEXT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT FK_profile_educations_profile
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

CREATE INDEX IDX_educations_profile ON profile_educations (profile_id);