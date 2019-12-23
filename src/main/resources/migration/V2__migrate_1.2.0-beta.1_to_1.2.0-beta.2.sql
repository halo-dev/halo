-- Migrate 1.2.0-beta.1 to 1.2.0-beta.2

-- Migrate journals Table
alter table journals modify content text not null;
alter table journals add source_content varchar(1023) default '' not null;
update journals set `source_content`=`content`