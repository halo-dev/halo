-- Migrate 1.3.0-beta.2 to 1.3.0-beta.3

-- Migrate options Table
alter table options modify option_value longtext not null;

-- Migrate theme_settings Table
alter table theme_settings modify setting_value longtext not null;