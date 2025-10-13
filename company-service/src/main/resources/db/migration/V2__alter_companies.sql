DROP INDEX IDX_companies_created_by ON companies;


ALTER TABLE companies
    MODIFY COLUMN created_by VARCHAR(255) NULL;


CREATE INDEX IDX_companies_created_by
    ON companies (created_by);
