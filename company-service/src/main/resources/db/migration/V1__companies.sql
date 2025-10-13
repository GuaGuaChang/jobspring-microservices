CREATE TABLE IF NOT EXISTS companies (
                                         id           BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                                         name         VARCHAR(255) NOT NULL,
    website      VARCHAR(255) NULL,
    size         INT          NULL,
    logo_url     VARCHAR(512) NULL,
    description  TEXT         NULL,
    created_by   BIGINT UNSIGNED NULL COMMENT 'Soft reference to usersvc userId (no FK)',


    KEY IDX_companies_created_by (created_by),

    CONSTRAINT UK_companies_name UNIQUE (name)
    )
    ENGINE=InnoDB
    DEFAULT CHARSET=utf8mb4
    COLLATE=utf8mb4_0900_ai_ci;