-- Migrate 1.3.0-beta.1 to 1.3.0-beta.2

-- Migrate posts Table
update posts set `slug`=`url`;
alter table posts modify slug varchar(255) not null;
alter table posts modify url varchar(255) null;
alter table posts modify summary longtext;

-- Migrate categories Table
update categories set `slug`=`slug_name`;
alter table categories modify slug varchar(255) not null;
alter table categories modify name varchar(255) not null;
alter table categories modify slug_name varchar(50) null;

-- Migrate tags Table
update tags set `slug`=`slug_name`;
alter table tags modify slug varchar(50) not null;
alter table tags modify slug_name varchar(255) null;