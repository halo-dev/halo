package run.halo.app.controller.admin.api;

import cn.hutool.core.text.StrBuilder;
import cn.hutool.crypto.SecureUtil;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import run.halo.app.cache.lock.CacheLock;
import run.halo.app.event.logger.LogEvent;
import run.halo.app.exception.BadRequestException;
import run.halo.app.model.entity.BaseComment;
import run.halo.app.model.entity.Category;
import run.halo.app.model.entity.Post;
import run.halo.app.model.entity.User;
import run.halo.app.model.enums.LogType;
import run.halo.app.model.enums.PostStatus;
import run.halo.app.model.params.*;
import run.halo.app.model.properties.BlogProperties;
import run.halo.app.model.properties.PrimaryProperties;
import run.halo.app.model.properties.PropertyEnum;
import run.halo.app.model.support.BaseResponse;
import run.halo.app.model.support.CreateCheck;
import run.halo.app.service.*;
import run.halo.app.utils.ValidationUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Installation controller.
 *
 * @author ryanwang
 * @date 2019-03-17
 */
@Slf4j
@Controller
@RequestMapping("/api/admin/installations")
public class InstallController {

    private final UserService userService;

    private final CategoryService categoryService;

    private final PostService postService;

    private final PostCommentService postCommentService;

    private final OptionService optionService;

    private final MenuService menuService;

    private final ApplicationEventPublisher eventPublisher;

    public InstallController(UserService userService,
                             CategoryService categoryService,
                             PostService postService,
                             PostCommentService postCommentService,
                             OptionService optionService,
                             MenuService menuService,
                             ApplicationEventPublisher eventPublisher) {
        this.userService = userService;
        this.categoryService = categoryService;
        this.postService = postService;
        this.postCommentService = postCommentService;
        this.optionService = optionService;
        this.menuService = menuService;
        this.eventPublisher = eventPublisher;
    }

    @PostMapping
    @ResponseBody
    @CacheLock
    @ApiOperation("Initializes the blog")
    public BaseResponse<String> installBlog(@RequestBody InstallParam installParam) {
        // Validate manually
        ValidationUtils.validate(installParam, CreateCheck.class);

        // Check is installed
        boolean isInstalled = optionService.getByPropertyOrDefault(PrimaryProperties.IS_INSTALLED, Boolean.class, false);

        if (isInstalled) {
            throw new BadRequestException("该博客已初始化，不能再次安装！");
        }

        // Initialize settings
        initSettings(installParam);

        // Create default user
        User user = createUser(installParam);

        // Create default category
        Category category = createDefaultCategoryIfAbsent();

        // Create default post
        Post post = createDefaultPostIfAbsent(category);

        // Create default postComment
        createDefaultComment(post);

        // Create default menu
        createDefaultMenu();

        eventPublisher.publishEvent(
                new LogEvent(this, user.getId().toString(), LogType.BLOG_INITIALIZED, "博客已成功初始化")
        );

        return BaseResponse.ok("安装完成！");
    }

    private void createDefaultMenu() {
        long menuCount = menuService.count();

        if (menuCount > 0) {
            return;
        }

        MenuParam menuIndex = new MenuParam();

        menuIndex.setName("首页");
        menuIndex.setUrl("/");
        menuIndex.setPriority(1);

        menuService.create(menuIndex.convertTo());

        MenuParam menuArchive = new MenuParam();

        menuArchive.setName("归档");
        menuArchive.setUrl("/archives");
        menuArchive.setPriority(2);
        menuService.create(menuArchive.convertTo());
    }


    @Nullable
    private BaseComment createDefaultComment(@Nullable Post post) {
        if (post == null) {
            return null;
        }

        PostCommentParam commentParam = new PostCommentParam();
        commentParam.setAuthor("Halo Bot");
        commentParam.setAuthorUrl("https://halo.run");
        commentParam.setContent("欢迎使用 Halo，这是你的第一条评论。");
        commentParam.setEmail("halo@halo.run");
        commentParam.setPostId(post.getId());
        return postCommentService.create(commentParam.convertTo());
    }

    @Nullable
    private Post createDefaultPostIfAbsent(@Nullable Category category) {
        long publishedCount = postService.countByStatus(PostStatus.PUBLISHED);
        if (publishedCount > 0) {
            return null;
        }

        PostParam postParam = new PostParam();
        postParam.setUrl("hello-halo");
        postParam.setTitle("Hello Halo");
        postParam.setStatus(PostStatus.PUBLISHED);
        postParam.setOriginalContent("## Hello Halo!\n" +
                "\n" +
                "感谢使用 [Halo](https://github.com/halo-dev/halo) 进行创作，请删除该文章开始吧！");

        if (category != null) {
            Set<Integer> categoryIds = new HashSet<>();
            categoryIds.add(category.getId());
            postParam.setCategoryIds(categoryIds);
        }
        return postService.createOrUpdateBy(postParam.convertTo());
    }

    @Nullable
    private Category createDefaultCategoryIfAbsent() {
        long categoryCount = categoryService.count();
        if (categoryCount > 0) {
            return null;
        }

        CategoryParam category = new CategoryParam();
        category.setName("未分类");
        category.setSlugName("default");
        category.setDescription("未分类");
        ValidationUtils.validate(category);
        return categoryService.create(category.convertTo());
    }

    private User createUser(InstallParam installParam) {
        // Get user
        return userService.getCurrentUser().map(user -> {
            // Update this user
            installParam.update(user);
            // Set password manually
            userService.setPassword(user, installParam.getPassword());
            // Update user
            return userService.update(user);
        }).orElseGet(() -> {
            StrBuilder gravatar = new StrBuilder("//cn.gravatar.com/avatar/");
            gravatar.append(SecureUtil.md5(installParam.getEmail()));
            gravatar.append("?s=256&d=mm");
            installParam.setAvatar(gravatar.toString());
            return userService.createBy(installParam);
        });
    }

    private void initSettings(InstallParam installParam) {
        // Init default properties
        Map<PropertyEnum, String> properties = new HashMap<>(11);
        properties.put(PrimaryProperties.IS_INSTALLED, Boolean.TRUE.toString());
        properties.put(BlogProperties.BLOG_LOCALE, installParam.getLocale());
        properties.put(BlogProperties.BLOG_TITLE, installParam.getTitle());
        properties.put(BlogProperties.BLOG_URL, StringUtils.isBlank(installParam.getUrl()) ?
                optionService.getBlogBaseUrl() : installParam.getUrl());
        properties.put(PrimaryProperties.BIRTHDAY, String.valueOf(System.currentTimeMillis()));

        // Create properties
        optionService.saveProperties(properties);
    }

}
