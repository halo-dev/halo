-- Migrate * to 1.1.3

-- Migrate options Table
update options set option_key = 'oss_ali_domain_protocol' where option_key = 'oss_aliyun_domain_protocol';
update options set option_key = 'oss_ali_domain' where option_key = 'oss_aliyun_domain';
update options set option_key = 'oss_ali_endpoint' where option_key = 'oss_aliyun_endpoint';
update options set option_key = 'oss_ali_bucket_name' where option_key = 'oss_aliyun_bucket_name';
update options set option_key = 'oss_ali_access_key' where option_key = 'oss_aliyun_access_key';
update options set option_key = 'oss_ali_access_secret' where option_key = 'oss_aliyun_access_secret';
update options set option_key = 'oss_ali_style_rule' where option_key = 'oss_aliyun_style_rule';
update options set option_key = 'oss_ali_thumbnail_style_rule' where option_key = 'oss_aliyun_thumbnail_style_rule';

update options set option_key = 'bos_baidu_domain_protocol' where option_key = 'bos_baiduyun_domain_protocol';
update options set option_key = 'bos_baidu_domain' where option_key = 'bos_baiduyun_domain';
update options set option_key = 'bos_baidu_endpoint' where option_key = 'bos_baiduyun_endpoint';
update options set option_key = 'bos_baidu_bucket_name' where option_key = 'bos_baiduyun_bucket_name';
update options set option_key = 'bos_baidu_access_key' where option_key = 'bos_baiduyun_access_key';
update options set option_key = 'bos_baidu_secret_key' where option_key = 'bos_baiduyun_secret_key';
update options set option_key = 'bos_baidu_style_rule' where option_key = 'bos_baiduyun_style_rule';
update options set option_key = 'bos_baidu_thumbnail_style_rule' where option_key = 'bos_baiduyun_thumbnail_style_rule';

update options set option_key = 'cos_tencent_domain_protocol' where option_key = 'cos_tencentyun_domain_protocol';
update options set option_key = 'cos_tencent_domain' where option_key = 'cos_tencentyun_domain';
update options set option_key = 'cos_tencent_region' where option_key = 'cos_tencentyun_region';
update options set option_key = 'cos_tencent_bucket_name' where option_key = 'cos_tencentyun_bucket_name';
update options set option_key = 'cos_tencent_secret_id' where option_key = 'cos_tencentyun_secret_id';
update options set option_key = 'cos_tencent_secret_key' where option_key = 'cos_tencentyun_secret_key';
update options set option_key = 'cos_tencent_thumbnail_style_rule' where option_key = 'cos_tencentyun_thumbnail_style_rule';

-- Migrate attachments Table
alter table attachments modify media_type varchar (127) not null;

-- Migrate posts Table
alter table posts modify `original_content` longtext not null;
alter table posts modify `format_content` longtext not null;