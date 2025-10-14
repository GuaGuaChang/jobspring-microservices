CREATE
DATABASE IF NOT EXISTS jobspring_auth DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

CREATE
USER IF NOT EXISTS 'jobspring_user'@'%' IDENTIFIED BY 'jobspring_pass';
GRANT ALL PRIVILEGES ON jobspring_auth.* TO
'jobspring_user'@'%';
FLUSH
PRIVILEGES;

CREATE
DATABASE IF NOT EXISTS jobspring_company DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

CREATE
USER IF NOT EXISTS 'jobspring_user'@'%' IDENTIFIED BY 'jobspring_pass';
GRANT ALL PRIVILEGES ON jobspring_company.* TO
'jobspring_user'@'%';
FLUSH
PRIVILEGES;

CREATE
DATABASE IF NOT EXISTS jobspring_user DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

CREATE
USER IF NOT EXISTS 'jobspring_user'@'%' IDENTIFIED BY 'jobspring_pass';
GRANT ALL PRIVILEGES ON jobspring_user.* TO
'jobspring_user'@'%';
FLUSH
PRIVILEGES;

CREATE
DATABASE IF NOT EXISTS jobspring_job DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

CREATE
USER IF NOT EXISTS 'jobspring_user'@'%' IDENTIFIED BY 'jobspring_pass';
GRANT ALL PRIVILEGES ON jobspring_job.* TO
'jobspring_user'@'%';
FLUSH
PRIVILEGES;

CREATE
DATABASE IF NOT EXISTS jobspring_company DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

CREATE
USER IF NOT EXISTS 'jobspring_user'@'%' IDENTIFIED BY 'jobspring_pass';
GRANT ALL PRIVILEGES ON jobspring_company.* TO
'jobspring_user'@'%';
FLUSH
PRIVILEGES;

CREATE
DATABASE IF NOT EXISTS jobspring_user DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

CREATE
USER IF NOT EXISTS 'jobspring_user'@'%' IDENTIFIED BY 'jobspring_pass';
GRANT ALL PRIVILEGES ON jobspring_user.* TO
'jobspring_user'@'%';
FLUSH
PRIVILEGES;

CREATE
DATABASE IF NOT EXISTS jobspring_application DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

CREATE
USER IF NOT EXISTS 'jobspring_user'@'%' IDENTIFIED BY 'jobspring_pass';
GRANT ALL PRIVILEGES ON jobspring_application.* TO
'jobspring_user'@'%';
FLUSH
PRIVILEGES;
