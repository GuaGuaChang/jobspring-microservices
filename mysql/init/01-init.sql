CREATE
DATABASE IF NOT EXISTS jobspring_auth DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

CREATE
USER IF NOT EXISTS 'jobspring_user'@'%' IDENTIFIED BY 'jobspring_pass';
GRANT ALL PRIVILEGES ON jobspring_auth.* TO
'jobspring_user'@'%';
FLUSH
PRIVILEGES;