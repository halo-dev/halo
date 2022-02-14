-- Migrate categories Table
ALTER TABLE `categories` modify `sort` int NULL COMMENT '排序';