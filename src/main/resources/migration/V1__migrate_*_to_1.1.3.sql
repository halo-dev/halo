-- Migrate * to 1.1.3

-- Migrate options Table
UPDATE OPTIONS SET OPTION_KEY = 'oss_ali_domain_protocol' WHERE OPTION_KEY = 'oss_aliyun_domain_protocol';
UPDATE OPTIONS SET OPTION_KEY = 'oss_ali_domain' WHERE OPTION_KEY = 'oss_aliyun_domain';
UPDATE OPTIONS SET OPTION_KEY = 'oss_ali_endpoint' WHERE OPTION_KEY = 'oss_aliyun_endpoint';
UPDATE OPTIONS SET OPTION_KEY = 'oss_ali_bucket_name' WHERE OPTION_KEY = 'oss_aliyun_bucket_name';
UPDATE OPTIONS SET OPTION_KEY = 'oss_ali_access_key' WHERE OPTION_KEY = 'oss_aliyun_access_key';
UPDATE OPTIONS SET OPTION_KEY = 'oss_ali_access_secret' WHERE OPTION_KEY = 'oss_aliyun_access_secret';
UPDATE OPTIONS SET OPTION_KEY = 'oss_ali_style_rule' WHERE OPTION_KEY = 'oss_aliyun_style_rule';
UPDATE OPTIONS SET OPTION_KEY = 'oss_ali_thumbnail_style_rule' WHERE OPTION_KEY = 'oss_aliyun_thumbnail_style_rule';

UPDATE OPTIONS SET OPTION_KEY = 'bos_baidu_domain_protocol' WHERE OPTION_KEY = 'bos_baiduyun_domain_protocol';
UPDATE OPTIONS SET OPTION_KEY = 'bos_baidu_domain' WHERE OPTION_KEY = 'bos_baiduyun_domain';
UPDATE OPTIONS SET OPTION_KEY = 'bos_baidu_endpoint' WHERE OPTION_KEY = 'bos_baiduyun_endpoint';
UPDATE OPTIONS SET OPTION_KEY = 'bos_baidu_bucket_name' WHERE OPTION_KEY = 'bos_baiduyun_bucket_name';
UPDATE OPTIONS SET OPTION_KEY = 'bos_baidu_access_key' WHERE OPTION_KEY = 'bos_baiduyun_access_key';
UPDATE OPTIONS SET OPTION_KEY = 'bos_baidu_secret_key' WHERE OPTION_KEY = 'bos_baiduyun_secret_key';
UPDATE OPTIONS SET OPTION_KEY = 'bos_baidu_style_rule' WHERE OPTION_KEY = 'bos_baiduyun_style_rule';
UPDATE OPTIONS SET OPTION_KEY = 'bos_baidu_thumbnail_style_rule' WHERE OPTION_KEY = 'bos_baiduyun_thumbnail_style_rule';

UPDATE OPTIONS SET OPTION_KEY = 'cos_tencent_domain_protocol' WHERE OPTION_KEY = 'cos_tencentyun_domain_protocol';
UPDATE OPTIONS SET OPTION_KEY = 'cos_tencent_domain' WHERE OPTION_KEY = 'cos_tencentyun_domain';
UPDATE OPTIONS SET OPTION_KEY = 'cos_tencent_region' WHERE OPTION_KEY = 'cos_tencentyun_region';
UPDATE OPTIONS SET OPTION_KEY = 'cos_tencent_bucket_name' WHERE OPTION_KEY = 'cos_tencentyun_bucket_name';
UPDATE OPTIONS SET OPTION_KEY = 'cos_tencent_secret_id' WHERE OPTION_KEY = 'cos_tencentyun_secret_id';
UPDATE OPTIONS SET OPTION_KEY = 'cos_tencent_secret_key' WHERE OPTION_KEY = 'cos_tencentyun_secret_key';
UPDATE OPTIONS SET OPTION_KEY = 'cos_tencent_thumbnail_style_rule' WHERE OPTION_KEY = 'cos_tencentyun_thumbnail_style_rule';