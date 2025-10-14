CREATE TABLE IF NOT EXISTS `applications`
(
    `id`
    BIGINT
    UNSIGNED
    NOT
    NULL
    AUTO_INCREMENT,
    `job_id`
    BIGINT
    UNSIGNED
    NOT
    NULL,
    `user_id`
    BIGINT
    UNSIGNED
    NOT
    NULL,
    `profile_id`
    BIGINT
    UNSIGNED
    NULL
    DEFAULT
    NULL
    COMMENT
    'Snapshot/Binding',
    `status`
    INT
    NOT
    NULL,
    `applied_at`
    DATETIME
    NULL
    DEFAULT
    NULL,
    `resume_profile`
    LONGTEXT
    NULL,
    `resume_url`
    LONGTEXT
    NULL,

    PRIMARY
    KEY
(
    `id`
),
    UNIQUE KEY `UK_app_unique`
(
    `job_id`,
    `user_id`
),
    KEY `IDX_apps_job`
(
    `job_id`
),
    KEY `IDX_apps_user`
(
    `user_id`
),
    KEY `IDX_apps_status`
(
    `status`
),
    KEY `IDX_apps_applied`
(
    `applied_at`
),
    KEY `IDX_apps_profile`
(
    `profile_id`
)
    ) ENGINE=InnoDB
    DEFAULT CHARSET=utf8mb4
    COLLATE =utf8mb4_0900_ai_ci
    ROW_FORMAT= DYNAMIC;
