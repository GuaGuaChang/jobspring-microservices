CREATE TABLE IF NOT EXISTS profiles
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
    NULL
    COMMENT
    '用户ID（逻辑引用 auth-service）',
    summary
    LONGTEXT
    NULL
    COMMENT
    '个人简介',
    visibility
    INT
    NOT
    NULL
    DEFAULT
    0
    COMMENT
    '0=私有 1=公司可见 2=公开',
    file_url
    VARCHAR
(
    512
) NULL COMMENT '简历文件 URL 或 S3 路径',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
    ) ENGINE=InnoDB
    DEFAULT CHARSET=utf8mb4
    COLLATE =utf8mb4_0900_ai_ci;

CREATE INDEX IDX_profiles_user ON profiles (user_id);
CREATE INDEX IDX_profiles_visibility ON profiles (visibility);