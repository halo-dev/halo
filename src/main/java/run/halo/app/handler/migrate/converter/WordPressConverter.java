package run.halo.app.handler.migrate.converter;

import run.halo.app.handler.migrate.support.vo.PostVO;
import run.halo.app.handler.migrate.support.wordpress.*;
import run.halo.app.handler.migrate.utils.RelationMapperUtils;
import run.halo.app.model.entity.BaseComment;
import run.halo.app.model.entity.BasePost;
import run.halo.app.model.entity.Category;
import run.halo.app.model.entity.Tag;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * WordPress博客数据迁移转换器
 *
 * @author guqing
 * @date 2020-01-18 16:50
 */
public class WordPressConverter implements Converter<Rss, List<PostVO>> {

    @Override
    public List<PostVO> converterFromDto(Rss rss) {
        return converterFromDto(rss, this::apply);
    }

    /**
     * 自定义转换规则
     */
    public List<PostVO> apply(Rss rss) {
        if (Objects.isNull(rss) || Objects.isNull(rss.getChannel())) {
            return new ArrayList<>();
        }

        Channel channel = rss.getChannel();
        List<Item> items = channel.getItems();

        if (items == null) {
            return new ArrayList<>();
        }
        return getBasePost(items);
    }

    private List<PostVO> getBasePost(List<Item> items) {
        List<PostVO> posts = new ArrayList<>();
        if (items == null) {
            return posts;
        }

        for (Item item : items) {
            PostVO postVo = new PostVO();
            // 设置文章
            BasePost post = getBasePostFromItem(item);
            postVo.setBasePost(post);

            // 获取标签和分类
            List<WpCategory> categories = item.getCategories();
            List<Category> categoryModelList = new ArrayList<>();
            List<Tag> tags = new ArrayList<>();
            if (categories != null) {
                categories.forEach(category -> {
                    String domain = category.getDomain();
                    if ("post_tag".equals(domain)) {
                        Tag tag = RelationMapperUtils.convertFrom(category, Tag.class);
                        tags.add(tag);
                    } else if ("category".equals(domain)) {
                        Category categoryModel = RelationMapperUtils.convertFrom(category, Category.class);
                        categoryModelList.add(categoryModel);
                    }
                });
            }
            // 设置标签和分类
            postVo.setCategories(categoryModelList);
            postVo.setTags(tags);

            // 设置评论
            List<BaseComment> comments = getCommentsFromItem(item);
            postVo.setComments(comments);

            posts.add(postVo);
        }

        return posts;
    }

    private List<BaseComment> getCommentsFromItem(Item item) {
        List<BaseComment> baseComments = new ArrayList<>();
        if (Objects.isNull(item) || Objects.isNull(item.getComments())) {
            return baseComments;
        }

        List<Comment> comments = item.getComments();
        for (Comment comment : comments) {
            BaseComment baseComment = RelationMapperUtils.convertFrom(comment, BaseComment.class);
            baseComments.add(baseComment);
        }

        return baseComments;
    }

    private BasePost getBasePostFromItem(Item item) {
        BasePost post = RelationMapperUtils.convertFrom(item, BasePost.class);
        String postDate = item.getPostDate();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        if (postDate != null) {
            LocalDateTime dateTime = LocalDateTime.parse(postDate, formatter);
            Date date = Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
            post.setEditTime(date);
            // 设置url为文章编辑时的时间毫秒数
            post.setUrl(post.getEditTime() + "");
        } else {
            post.setUrl(System.currentTimeMillis() + "");
        }
        return post;
    }
}
