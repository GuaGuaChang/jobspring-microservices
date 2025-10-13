ALTER TABLE accounts
    ADD COLUMN company_id BIGINT UNSIGNED NULL AFTER full_name,
  ADD KEY idx_accounts_company_id (company_id);