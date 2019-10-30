package run.halo.app.handler.migrate;

import lombok.extern.slf4j.Slf4j;
import org.dom4j.Element;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;
import run.halo.app.exception.ServiceException;
import run.halo.app.model.entity.BasePost;
import run.halo.app.model.entity.Category;
import run.halo.app.model.entity.Post;
import run.halo.app.model.entity.Tag;
import run.halo.app.model.enums.MigrateType;
import run.halo.app.service.*;
import run.halo.app.utils.MarkdownUtils;
import run.halo.app.utils.WordPressMigrateUtils;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * WordPress migrate handler
 *
 * @author ryanwang
 * @date 2019-10-28
 */
@Slf4j
@Component
@SuppressWarnings("unchecked")
public class WordPressMigrateHandler implements MigrateHandler {

    private final AttachmentService attachmentService;

    private final PostService postService;

    private final LinkService linkService;

    private final MenuService menuService;

    private final CategoryService categoryService;

    private final TagService tagService;

    private final PostCommentService postCommentService;

    private final SheetCommentService sheetCommentService;

    private final SheetService sheetService;

    private final PhotoService photoService;

    private final PostCategoryService postCategoryService;

    private final PostTagService postTagService;

    public WordPressMigrateHandler(AttachmentService attachmentService,
                                   PostService postService,
                                   LinkService linkService,
                                   MenuService menuService,
                                   CategoryService categoryService,
                                   TagService tagService,
                                   PostCommentService postCommentService,
                                   SheetCommentService sheetCommentService,
                                   SheetService sheetService,
                                   PhotoService photoService,
                                   PostCategoryService postCategoryService,
                                   PostTagService postTagService) {
        this.attachmentService = attachmentService;
        this.postService = postService;
        this.linkService = linkService;
        this.menuService = menuService;
        this.categoryService = categoryService;
        this.tagService = tagService;
        this.postCommentService = postCommentService;
        this.sheetCommentService = sheetCommentService;
        this.sheetService = sheetService;
        this.photoService = photoService;
        this.postCategoryService = postCategoryService;
        this.postTagService = postTagService;
    }

    @Override
    public void migrate(MultipartFile file) {
        try {
            String migrationContent = FileCopyUtils.copyToString(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
            Element rootElement = WordPressMigrateUtils.getRootElement(new FileInputStream(migrationContent));
            Map<String, Object> resultSetMapping = WordPressMigrateUtils.getResultSetMapping(rootElement);

            // Handle categories
            List<Category> categories = handleCategories(resultSetMapping.get("wp:category"));

            // Handle tags
            List<Tag> tags = handleTags(resultSetMapping.get("wp:tag"));

            // Handle posts
            List<BasePost> posts = handlePosts(resultSetMapping.get("item"));

            log.debug("Migrated posts: [{}]", posts);
        } catch (Exception e) {
            throw new ServiceException("WordPress 导出文件 " + file.getOriginalFilename() + " 读取失败", e);
        }
    }

    private List<Category> handleCategories(@Nullable Object categoriesObject) {

        if (!(categoriesObject instanceof List)) {
            return Collections.emptyList();
        }

        List<Object> categoryObjectList = (List<Object>) categoriesObject;

        List<Category> result = new LinkedList<>();

        categoryObjectList.forEach(categoryObject -> {

            if (!(categoryObject instanceof Map)) {
                return;
            }

            Map<String, Object> categoryMap = (Map<String, Object>) categoryObject;

            String slugName = categoryMap.getOrDefault("wp:category_nicename", "").toString();

            Category category = categoryService.getBySlugName(slugName);

            if (null == category) {
                category = new Category();
                category.setName(categoryMap.getOrDefault("wp:cat_name", "").toString());
                category.setSlugName(slugName);
                category = categoryService.create(category);
            }

            try {
                result.add(category);
            } catch (Exception e) {
                log.warn("Failed to migrate a category", e);
            }
        });

        return result;
    }

    private List<Tag> handleTags(@Nullable Object tagsObject) {

        if (!(tagsObject instanceof List)) {
            return Collections.emptyList();
        }

        List<Object> tagObjectList = (List<Object>) tagsObject;

        List<Tag> result = new LinkedList<>();

        tagObjectList.forEach(tagObject -> {
            if (!(tagObject instanceof Map)) {
                return;
            }

            Map<String, Object> tagMap = (Map<String, Object>) tagObject;

            String slugName = tagMap.getOrDefault("wp:tag_slug", "").toString();

            Tag tag = tagService.getBySlugName(slugName);

            if (null == tag) {
                tag = new Tag();
                tag.setName(tagMap.getOrDefault("wp:tag_name", "").toString());
                tag.setSlugName(slugName);
                tag = tagService.create(tag);
            }

            try {
                result.add(tag);
            } catch (Exception e) {
                log.warn("Failed to migrate a tag", e);
            }
        });

        return result;
    }

    @NonNull
    private List<BasePost> handlePosts(@Nullable Object postsObject) {
        if (!(postsObject instanceof List)) {
            return Collections.emptyList();
        }

        List<Object> postObjectList = (List<Object>) postsObject;

        List<BasePost> result = new LinkedList<>();

        postObjectList.forEach(postObject -> {
            if (!(postObject instanceof Map)) {
                return;
            }

            Map<String, Object> postMap = (Map<String, Object>) postObject;

            BasePost post = new BasePost();
            post.setTitle(postMap.getOrDefault("title", "").toString());
            post.setUrl(postMap.getOrDefault("wp:post_name", "").toString());
            post.setOriginalContent(MarkdownUtils.renderMarkdown(postMap.getOrDefault("content:encoded", "").toString()));
            post.setFormatContent(postMap.getOrDefault("content:encoded", "").toString());
            post.setSummary(postMap.getOrDefault("excerpt:encoded", "").toString());

            String url = postMap.getOrDefault("wp:post_name", "").toString();

            Post temp = postService.getByUrl(url);

            if (temp != null) {
                post.setUrl(post.getUrl() + "_1");
            }


        });
        return null;
    }

    @Override
    public boolean supportType(MigrateType type) {
        return MigrateType.WORDPRESS.equals(type);
    }
}
