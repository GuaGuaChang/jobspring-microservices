SET
@col_exists := (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'applications'
    AND COLUMN_NAME = 'company_id'
);

SET
@ddl := IF(
  @col_exists = 0,
  'ALTER TABLE applications ADD COLUMN company_id BIGINT NULL AFTER user_id',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;


SET
@idx_exists := (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'applications'
    AND INDEX_NAME = 'idx_apps_company'
);

SET
@ddl := IF(
  @idx_exists = 0,
  'CREATE INDEX idx_apps_company ON applications (company_id)',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

