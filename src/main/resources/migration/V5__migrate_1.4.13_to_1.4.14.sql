-- Migrate 1.4.13 to 1.4.14

-- Migrate comments Table
alter table comments modify email VARCHAR(255);