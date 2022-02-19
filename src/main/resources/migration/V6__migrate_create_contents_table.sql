-- Migrate post content to contents table
INSERT INTO contents(post_id, status, patch_log_id, head_patch_log_id, content, original_content, create_time,
                     update_time)
SELECT id,
       status,
       id,
       id,
       format_content,
       original_content,
       create_time,
       update_time
FROM posts;

-- Create content_patch_logs record by posts and contents table record
INSERT INTO content_patch_logs(id, post_id, content_diff, original_content_diff, version, status, publish_time,
                               source_id, create_time, update_time)
SELECT p.id,
       p.id,
       c.content,
       c.original_content,
       1,
       p.status,
       p.create_time,
       0,
       p.create_time,
       p.update_time
FROM contents c
         INNER JOIN posts p ON p.id = c.post_id;

-- Allow the original_content to be null
alter table posts
    modify format_content longtext null;
alter table posts
    modify original_content longtext null;
