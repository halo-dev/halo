package run.halo.app.handler.migrate.converter;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;
import org.springframework.util.DigestUtils;
import run.halo.app.handler.migrate.support.vo.PostVO;
import run.halo.app.handler.migrate.support.wordpress.*;
import run.halo.app.model.entity.BaseComment;
import run.halo.app.model.entity.BasePost;
import run.halo.app.model.entity.Category;
import run.halo.app.model.entity.Tag;
import run.halo.app.model.enums.CommentStatus;
import run.halo.app.model.enums.PostCreateFrom;
import run.halo.app.model.enums.PostStatus;
import run.halo.app.utils.MarkdownUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

/**
 * WordPress博客数据迁移转换器
 * @author guqing
 * @date 2020-01-18 16:50
 */
public class WordPressConverter extends Converter<Rss, List<PostVO>> {

    public WordPressConverter() {
        super(new PostVoFunction());
    }

    /**
     * 自定义转换规则
     */
    private static class PostVoFunction implements Function<Rss, List<PostVO>> {
        @Override
        public List<PostVO> apply(Rss rss) {
            Channel channel = rss.getChannel();
            List<Item> items = channel.getItems();
            Assert.notNull(items, "解析出错,文章item项不能为null");

            return  getBasePost(items);
        }

        private List<PostVO> getBasePost(List<Item> items) {
            List<PostVO> posts = new ArrayList<>();
            items.forEach(item -> {
                PostVO postVo = new PostVO();

                // 设置文章
                BasePost post = getBasePostFromItem(item);
                postVo.setBasePost(post);

                // 获取标签和分类
                List<WpCategory> categories = item.getCategories();
                List<Category> categoryModelList = new ArrayList<>();
                List<Tag> tags = new ArrayList<>();
                categories.forEach(category -> {
                    String domain = category.getDomain();
                    if(StringUtils.equals("post_tag", domain)) {
                        Tag tag = new Tag();
                        tag.setName(category.getDomain());
                        tag.setSlugName(category.getNicename());
                        tags.add(tag);
                    } else if(StringUtils.equals("category", domain)) {
                        Category categoryModel = new Category();
                        categoryModel.setName(domain);
                        categoryModel.setSlugName(category.getNicename());
                        categoryModelList.add(categoryModel);
                    }
                });
                // 设置标签和分类
                postVo.setCategories(categoryModelList);
                postVo.setTags(tags);

                // 设置评论
                List<BaseComment> comments = getCommentsFromItem(item);
                postVo.setComments(comments);

                posts.add(postVo);
            });

            return posts;
        }

        private List<BaseComment> getCommentsFromItem(Item item) {
            List<BaseComment> baseComments = new ArrayList<>();
            List<Comment> comments = item.getComments();
            comments.forEach(comment -> {
                BaseComment baseComment = new BaseComment();
                baseComment.setAllowNotification(true);
                baseComment.setAuthor(comment.getCommentAuthor());
                baseComment.setAuthorUrl(comment.getCommentAuthorUrl());
                baseComment.setContent(comment.getCommentContent());
                baseComment.setEmail(comment.getCommentAuthorEmail());
                if(StringUtils.isNotBlank(comment.getCommentAuthorEmail())) {
                    String md5DigestAsHex = DigestUtils.md5DigestAsHex(comment.getCommentAuthorEmail().getBytes());
                    baseComment.setGravatarMd5(md5DigestAsHex);
                }
                baseComment.setIpAddress(comment.getCommentAuthorIp());
                baseComment.setParentId(comment.getCommentParent());
                baseComment.setStatus(CommentStatus.PUBLISHED);
                baseComment.setTopPriority(0);

                baseComments.add(baseComment);
            });
            return baseComments;
        }
        private BasePost getBasePostFromItem(Item item) {
            BasePost post = new BasePost();
            post.setTitle(item.getTitle());
            post.setPassword(item.getPostPassword());
            post.setFormatContent(item.getContent());
            post.setOriginalContent(MarkdownUtils.renderMarkdown(item.getContent()));
            post.setLikes(0L);
            post.setCreateFrom(PostCreateFrom.ADMIN);

            String postDate = item.getPostDate();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            if(postDate == null) {
                post.setEditTime(new Date());
            } else {
                LocalDateTime dateTime = LocalDateTime.parse(postDate, formatter);
                Date date = Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
                post.setEditTime(date);
            }

            post.setStatus(PostStatus.PUBLISHED);

            // 设置url为文章编辑时的时间毫秒数
            Date editTime = post.getEditTime();
            post.setUrl(editTime.getTime() + "");

            post.setVisits(0L);
            post.setDeleted(false);
            post.setTopPriority(0);
            return post;
        }
    }
}
