-- Remove notnull for email in comments table

-- Migrate comments Table
alter table comments modify email VARCHAR(255);
