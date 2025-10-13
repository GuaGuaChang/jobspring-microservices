CREATE TABLE accounts
(
    id            BIGINT AUTO_INCREMENT NOT NULL,
    email         VARCHAR(255) NOT NULL,
    phone         VARCHAR(255) NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name     VARCHAR(255) NOT NULL,
    `role`        TINYINT      NOT NULL,
    is_active     BIT(1)       NOT NULL,
    CONSTRAINT pk_accounts PRIMARY KEY (id)
);

ALTER TABLE accounts
    ADD CONSTRAINT uc_accounts_email UNIQUE (email);