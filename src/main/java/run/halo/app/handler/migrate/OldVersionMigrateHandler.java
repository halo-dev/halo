package run.halo.app.handler.migrate;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;
import run.halo.app.exception.ServiceException;
import run.halo.app.model.entity.*;
import run.halo.app.model.enums.AttachmentType;
import run.halo.app.model.enums.CommentStatus;
import run.halo.app.model.enums.MigrateType;
import run.halo.app.model.enums.PostStatus;
import run.halo.app.service.*;
import run.halo.app.utils.BeanUtils;
import run.halo.app.utils.JsonUtils;
import run.halo.app.utils.ServiceUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Old version(0.4.4) migrate handler
 *
 * @author ryanwang
 * @author johnniang
 * @date 2019-10-28
 */
@Slf4j
@Component
@SuppressWarnings("unchecked")
public class OldVersionMigrateHandler implements MigrateHandler {

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

    public OldVersionMigrateHandler(AttachmentService attachmentService,
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
        // Get migration content
        try {
            String migrationContent = FileCopyUtils.copyToString(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));

            Object migrationObject = JsonUtils.jsonToObject(migrationContent, Object.class);

            if (migrationObject instanceof Map) {
                Map<String, Object> migrationMap = (Map<String, Object>) migrationObject;

                // Handle attachments
                List<Attachment> attachments = handleAttachments(migrationMap.get("attachments"));

                log.debug("Migrated attachments: [{}]", attachments);

                // Handle links
                List<Link> links = handleLinks(migrationMap.get("links"));

                log.debug("Migrated links: [{}]", links);

                // Handle galleries
                List<Photo> photos = handleGalleries(migrationMap.get("galleries"));

                log.debug("Migrated photos: [{}]", photos);

                // Handle menus
                List<Menu> menus = handleMenus(migrationMap.get("menus"));

                log.debug("Migrated menus: [{}]", menus);

                // Handle posts
                List<BasePost> posts = handleBasePosts(migrationMap.get("posts"));

                log.debug("Migrated posts: [{}]", posts);
            }
        } catch (IOException e) {
            throw new ServiceException("备份文件 " + file.getOriginalFilename() + " 读取失败", e);
        }
    }


    @NonNull
    private List<BasePost> handleBasePosts(@Nullable Object postsObject) {
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
            post.setTitle(postMap.getOrDefault("postTitle", "").toString());
            post.setUrl(postMap.getOrDefault("postUrl", "").toString());
            post.setOriginalContent(postMap.getOrDefault("postContentMd", "").toString());
            post.setFormatContent(postMap.getOrDefault("postContent", "").toString());
            post.setSummary(postMap.getOrDefault("postSummary", "").toString());
            post.setThumbnail(postMap.getOrDefault("postThumbnail", "").toString());
            post.setVisits(getLongOrDefault(postMap.getOrDefault("postViews", "").toString(), 0L));
            post.setDisallowComment(false);
            post.setTemplate(postMap.getOrDefault("customTpl", "").toString());

            // Set disallow comment
            Integer allowComment = getIntegerOrDefault(postMap.getOrDefault("allowComment", "1").toString(), 1);
            if (allowComment != 1) {
                post.setDisallowComment(true);
            }

            // Set create time
            Long createTime = getLongOrDefault(postMap.getOrDefault("postDate", "").toString(), 0L);
            if (createTime != 0L) {
                post.setCreateTime(new Date(createTime));
            }

            // Set update time
            Long updateTime = getLongOrDefault(postMap.getOrDefault("postUpdate", "").toString(), 0L);
            if (updateTime != 0L) {
                post.setUpdateTime(new Date(updateTime));
            }

            // Set status (default draft)
            Integer postStatus = getIntegerOrDefault(postMap.getOrDefault("postStatus", "").toString(), 1);
            if (postStatus == 0) {
                post.setStatus(PostStatus.PUBLISHED);
            } else if (postStatus == 1) {
                post.setStatus(PostStatus.DRAFT);
            } else {
                post.setStatus(PostStatus.RECYCLE);
            }

            String postType = postMap.getOrDefault("postType", "post").toString();

            try {
                if ("post".equalsIgnoreCase(postType)) {
                    // Handle post
                    result.add(handlePost(post, postMap));
                } else {
                    // Handle page
                    result.add(handleSheet(post, postMap));
                }
            } catch (Exception e) {
                log.warn("Failed to migrate a post or sheet", e);
                // Ignore this exception
            }
        });

        return result;
    }

    @NonNull
    private Post handlePost(@NonNull BasePost basePost, @NonNull Map<String, Object> postMap) {
        Post post = BeanUtils.transformFrom(basePost, Post.class);

        // Create it
        Post createdPost = postService.createOrUpdateBy(post);

        Object commentsObject = postMap.get("comments");
        Object categoriesObject = postMap.get("categories");
        Object tagsObject = postMap.get("tags");
        // Handle comments
        List<BaseComment> baseComments = handleComment(commentsObject, createdPost.getId());

        // Handle categories
        List<Category> categories = handleCategories(categoriesObject, createdPost.getId());

        log.debug("Migrated categories of post [{}]: [{}]", categories, createdPost.getId());

        // Handle tags
        List<Tag> tags = handleTags(tagsObject, createdPost.getId());

        log.debug("Migrated tags of post [{}]: [{}]", tags, createdPost.getId());

        List<PostComment> postComments = baseComments.stream()
                .map(baseComment -> BeanUtils.transformFrom(baseComment, PostComment.class))
                .collect(Collectors.toList());

        try {
            // Build virtual comment
            PostComment virtualPostComment = new PostComment();
            virtualPostComment.setId(0L);
            // Create comments
            createPostCommentRecursively(virtualPostComment, postComments);
        } catch (Exception e) {
            log.warn("Failed to create post comments for post with id " + createdPost.getId(), e);
            // Ignore this exception
        }

        return createdPost;
    }

    @NonNull
    private Sheet handleSheet(@NonNull BasePost basePost, @NonNull Map<String, Object> postMap) {
        Sheet sheet = BeanUtils.transformFrom(basePost, Sheet.class);

        // Create it
        Sheet createdSheet = sheetService.createOrUpdateBy(sheet);

        Object commentsObject = postMap.get("comments");
        // Handle comments
        List<BaseComment> baseComments = handleComment(commentsObject, createdSheet.getId());

        List<SheetComment> sheetComments = baseComments.stream()
                .map(baseComment -> BeanUtils.transformFrom(baseComment, SheetComment.class))
                .collect(Collectors.toList());

        // Create comments
        try {
            // Build virtual comment
            SheetComment virtualSheetComment = new SheetComment();
            virtualSheetComment.setId(0L);
            // Create comments
            createSheetCommentRecursively(virtualSheetComment, sheetComments);
        } catch (Exception e) {
            log.warn("Failed to create sheet comments for sheet with id " + createdSheet.getId(), e);
            // Ignore this exception
        }

        return createdSheet;
    }


    private void createPostCommentRecursively(@NonNull final PostComment parentComment, List<PostComment> postComments) {
        Long oldParentId = parentComment.getId();

        // Create parent
        if (!ServiceUtils.isEmptyId(parentComment.getId())) {
            PostComment createdComment = postCommentService.create(parentComment);
            log.debug("Created post comment: [{}]", createdComment);
            parentComment.setId(createdComment.getId());
        }

        if (CollectionUtils.isEmpty(postComments)) {
            return;
        }
        // Get all children
        List<PostComment> children = postComments.stream()
                .filter(postComment -> Objects.equals(oldParentId, postComment.getParentId()))
                .collect(Collectors.toList());


        // Set parent id again
        children.forEach(postComment -> postComment.setParentId(parentComment.getId()));

        // Remove children
        postComments.removeAll(children);

        // Create children recursively
        children.forEach(childComment -> createPostCommentRecursively(childComment, postComments));
    }

    private void createSheetCommentRecursively(@NonNull final SheetComment parentComment, List<SheetComment> sheetComments) {
        Long oldParentId = parentComment.getId();
        // Create parent
        if (!ServiceUtils.isEmptyId(parentComment.getId())) {
            SheetComment createComment = sheetCommentService.create(parentComment);
            parentComment.setId(createComment.getId());
        }

        if (CollectionUtils.isEmpty(sheetComments)) {
            return;
        }
        // Get all children
        List<SheetComment> children = sheetComments.stream()
                .filter(sheetComment -> Objects.equals(oldParentId, sheetComment.getParentId()))
                .collect(Collectors.toList());

        // Set parent id again
        children.forEach(postComment -> postComment.setParentId(parentComment.getId()));

        // Remove children
        sheetComments.removeAll(children);

        // Create children recursively
        children.forEach(childComment -> createSheetCommentRecursively(childComment, sheetComments));
    }

    private List<BaseComment> handleComment(@Nullable Object commentsObject, @NonNull Integer postId) {
        Assert.notNull(postId, "Post id must not be null");

        if (!(commentsObject instanceof List)) {
            return Collections.emptyList();
        }

        List<Object> commentObjectList = (List<Object>) commentsObject;

        List<BaseComment> result = new LinkedList<>();

        commentObjectList.forEach(commentObject -> {
            if (!(commentObject instanceof Map)) {
                return;
            }

            Map<String, Object> commentMap = (Map<String, Object>) commentObject;

            BaseComment baseComment = new BaseComment();
            baseComment.setId(getLongOrDefault(commentMap.getOrDefault("commentId", "").toString(), null));
            baseComment.setAuthor(commentMap.getOrDefault("commentAuthor", "").toString());
            baseComment.setEmail(commentMap.getOrDefault("commentAuthorEmail", "").toString());
            baseComment.setIpAddress(commentMap.getOrDefault("commentAuthorIp", "").toString());
            baseComment.setAuthorUrl(commentMap.getOrDefault("commentAuthorUrl", "").toString());
            baseComment.setGravatarMd5(commentMap.getOrDefault("commentAuthorAvatarMd5", "").toString());
            baseComment.setContent(commentMap.getOrDefault("commentContent", "").toString());
            baseComment.setUserAgent(commentMap.getOrDefault("commentAgent", "").toString());
            baseComment.setIsAdmin(getBooleanOrDefault(commentMap.getOrDefault("isAdmin", "").toString(), false));
            baseComment.setPostId(postId);
            baseComment.setParentId(getLongOrDefault(commentMap.getOrDefault("commentParent", "").toString(), 0L));

            // Set create date
            Long createTimestamp = getLongOrDefault(commentMap.getOrDefault("createDate", "").toString(), System.currentTimeMillis());
            baseComment.setCreateTime(new Date(createTimestamp));

            Integer commentStatus = getIntegerOrDefault(commentMap.getOrDefault("commentStatus", "").toString(), 1);
            if (commentStatus == 0) {
                baseComment.setStatus(CommentStatus.PUBLISHED);
            } else if (commentStatus == 1) {
                baseComment.setStatus(CommentStatus.AUDITING);
            } else {
                baseComment.setStatus(CommentStatus.RECYCLE);
            }

            result.add(baseComment);
        });

        return result;
    }

    @NonNull
    private List<Category> handleCategories(@Nullable Object categoriesObject, @NonNull Integer postId) {
        Assert.notNull(postId, "Post id must not be null");

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

            String slugName = categoryMap.getOrDefault("cateUrl", "").toString();

            Category category = categoryService.getBySlugName(slugName);

            if (null == category) {
                category = new Category();
                category.setName(categoryMap.getOrDefault("cateName", "").toString());
                category.setSlugName(slugName);
                category.setDescription(categoryMap.getOrDefault("cateDesc", "").toString());
                category = categoryService.create(category);
            }

            PostCategory postCategory = new PostCategory();
            postCategory.setCategoryId(category.getId());
            postCategory.setPostId(postId);
            postCategoryService.create(postCategory);

            try {
                result.add(category);
            } catch (Exception e) {
                log.warn("Failed to migrate a category", e);
            }
        });

        return result;
    }

    @NonNull
    private List<Tag> handleTags(@Nullable Object tagsObject, @NonNull Integer postId) {
        Assert.notNull(postId, "Post id must not be null");

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

            String slugName = tagMap.getOrDefault("tagUrl", "").toString();

            Tag tag = tagService.getBySlugName(slugName);

            if (null == tag) {
                tag = new Tag();
                tag.setName(tagMap.getOrDefault("tagName", "").toString());
                tag.setSlugName(slugName);
                tag = tagService.create(tag);
            }

            PostTag postTag = new PostTag();
            postTag.setTagId(tag.getId());
            postTag.setPostId(postId);
            postTagService.create(postTag);

            try {
                result.add(tag);
            } catch (Exception e) {
                log.warn("Failed to migrate a tag", e);
            }
        });

        return result;
    }

    @NonNull
    private List<Menu> handleMenus(@Nullable Object menusObject) {
        if (!(menusObject instanceof List)) {
            return Collections.emptyList();
        }

        List<Object> menuObjectList = (List<Object>) menusObject;

        List<Menu> result = new LinkedList<>();

        menuObjectList.forEach(menuObject -> {
            if (!(menuObject instanceof Map)) {
                return;
            }

            Map<String, Object> menuMap = (Map<String, Object>) menuObject;

            Menu menu = new Menu();

            menu.setName(menuMap.getOrDefault("menuName", "").toString());
            menu.setUrl(menuMap.getOrDefault("menuUrl", "").toString());
            // Set priority
            String sortString = menuMap.getOrDefault("menuSort", "0").toString();
            menu.setPriority(getIntegerOrDefault(sortString, 0));
            menu.setTarget(menuMap.getOrDefault("menuTarget", "_self").toString());
            menu.setIcon(menuMap.getOrDefault("menuIcon", "").toString());

            try {
                // Create menu
                result.add(menuService.create(menu));
            } catch (Exception e) {
                log.warn("Failed to migrate a menu", e);
            }
        });

        return result;
    }

    @NonNull
    private List<Photo> handleGalleries(@Nullable Object galleriesObject) {
        if (!(galleriesObject instanceof List)) {
            return Collections.emptyList();
        }

        List<Object> galleryObjectList = (List<Object>) galleriesObject;

        List<Photo> result = new LinkedList<>();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        galleryObjectList.forEach(galleryObject -> {
            if (!(galleriesObject instanceof Map)) {
                return;
            }

            Map<String, Object> galleryMap = (Map<String, Object>) galleryObject;

            Photo photo = new Photo();
            photo.setName(galleryMap.getOrDefault("galleryName", "").toString());
            photo.setDescription(galleryMap.getOrDefault("galleryDesc", "").toString());
            photo.setLocation(galleryMap.getOrDefault("galleryLocation", "").toString());
            photo.setThumbnail(galleryMap.getOrDefault("galleryThumbnailUrl", "").toString());
            photo.setUrl(galleryMap.getOrDefault("galleryUrl", "").toString());

            Object galleryDate = galleryMap.get("galleryDate");

            try {
                if (galleryDate != null) {
                    photo.setTakeTime(dateFormat.parse(galleryDate.toString()));
                }

                // Create it
                result.add(photoService.create(photo));
            } catch (Exception e) {
                log.warn("Failed to create a photo", e);
                // Ignore this exception
            }

        });

        return result;
    }

    @NonNull
    private List<Link> handleLinks(@Nullable Object linksObject) {
        if (!(linksObject instanceof List)) {
            return Collections.emptyList();
        }

        List<Object> linkObjectList = (List<Object>) linksObject;

        List<Link> result = new LinkedList<>();

        linkObjectList.forEach(linkObject -> {
            if (!(linkObject instanceof Map)) {
                return;
            }

            Map<String, Object> linkMap = (Map<String, Object>) linkObject;

            Link link = new Link();

            link.setName(linkMap.getOrDefault("linkName", "").toString());
            link.setUrl(linkMap.getOrDefault("linkUrl", "").toString());
            link.setLogo(linkMap.getOrDefault("linkPic", "").toString());
            link.setDescription(linkMap.getOrDefault("linkDesc", "").toString());
            try {
                result.add(linkService.create(link));
            } catch (Exception e) {
                log.warn("Failed to migrate a link", e);
            }
        });

        return result;
    }

    @NonNull
    private List<Attachment> handleAttachments(@Nullable Object attachmentsObject) {
        if (!(attachmentsObject instanceof List)) {
            return Collections.emptyList();
        }

        List<Object> attachmentObjectList = (List<Object>) attachmentsObject;

        List<Attachment> result = new LinkedList<>();

        attachmentObjectList.forEach(attachmentObject -> {
            if (!(attachmentObject instanceof Map)) {
                return;
            }

            Map<String, Object> attachmentMap = (Map<String, Object>) attachmentObject;
            // Convert to attachment param
            Attachment attachment = new Attachment();

            attachment.setName(attachmentMap.getOrDefault("attachName", "").toString());
            attachment.setPath(StringUtils.removeStart(attachmentMap.getOrDefault("attachPath", "").toString(), "/"));
            attachment.setThumbPath(attachmentMap.getOrDefault("attachSmallPath", "").toString());
            attachment.setMediaType(attachmentMap.getOrDefault("attachType", "").toString());
            attachment.setSuffix(StringUtils.removeStart(attachmentMap.getOrDefault("attachSuffix", "").toString(), "."));
            attachment.setSize(0L);

            if (StringUtils.startsWith(attachment.getPath(), "/upload")) {
                // Set this key
                attachment.setFileKey(attachment.getPath());
            }

            // Set location
            String attachLocation = attachmentMap.getOrDefault("attachLocation", "").toString();
            if (StringUtils.equalsIgnoreCase(attachLocation, "qiniu")) {
                attachment.setType(AttachmentType.QINIUOSS);
            } else if (StringUtils.equalsIgnoreCase(attachLocation, "upyun")) {
                attachment.setType(AttachmentType.UPOSS);
            } else {
                attachment.setType(AttachmentType.LOCAL);
            }

            try {
                // Save to db
                Attachment createdAttachment = attachmentService.create(attachment);

                result.add(createdAttachment);

            } catch (Exception e) {
                // Ignore this exception
                log.warn("Failed to migrate an attachment " + attachment.getPath(), e);
            }
        });

        return result;
    }

    @NonNull
    private Integer getIntegerOrDefault(@Nullable String numberString, @Nullable Integer defaultNumber) {
        try {
            return Integer.valueOf(numberString);
        } catch (Exception e) {
            // Ignore this exception
            return defaultNumber;
        }
    }

    @NonNull
    private Long getLongOrDefault(@Nullable String numberString, @Nullable Long defaultNumber) {
        try {
            return Long.valueOf(numberString);
        } catch (Exception e) {
            // Ignore this exception
            return defaultNumber;
        }
    }

    private Boolean getBooleanOrDefault(@Nullable String boolString, @Nullable Boolean defaultValue) {
        if (StringUtils.equalsIgnoreCase(boolString, "0")) {
            return false;
        }

        if (StringUtils.equalsIgnoreCase(boolString, "1")) {
            return true;
        }

        if (StringUtils.equalsIgnoreCase(boolString, "true")) {
            return true;
        }

        if (StringUtils.equalsIgnoreCase(boolString, "false")) {
            return false;
        }

        return defaultValue;
    }

    @Override
    public boolean supportType(MigrateType type) {
        return MigrateType.OLD_VERSION.equals(type);
    }
}
