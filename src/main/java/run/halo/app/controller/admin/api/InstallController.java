package run.halo.app.controller.admin.api;

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
import run.halo.app.model.entity.Category;
import run.halo.app.model.entity.PostComment;
import run.halo.app.model.entity.User;
import run.halo.app.model.enums.LogType;
import run.halo.app.model.enums.PostStatus;
import run.halo.app.model.params.*;
import run.halo.app.model.properties.BlogProperties;
import run.halo.app.model.properties.OtherProperties;
import run.halo.app.model.properties.PrimaryProperties;
import run.halo.app.model.properties.PropertyEnum;
import run.halo.app.model.support.BaseResponse;
import run.halo.app.model.support.CreateCheck;
import run.halo.app.model.vo.PostDetailVO;
import run.halo.app.service.*;
import run.halo.app.utils.ValidationUtils;

import java.util.*;

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

    private final SheetService sheetService;

    private final PostCommentService postCommentService;

    private final OptionService optionService;

    private final MenuService menuService;

    private final ApplicationEventPublisher eventPublisher;

    public InstallController(UserService userService,
                             CategoryService categoryService,
                             PostService postService,
                             SheetService sheetService,
                             PostCommentService postCommentService,
                             OptionService optionService,
                             MenuService menuService,
                             ApplicationEventPublisher eventPublisher) {
        this.userService = userService;
        this.categoryService = categoryService;
        this.postService = postService;
        this.sheetService = sheetService;
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
        PostDetailVO post = createDefaultPostIfAbsent(category);

        // Create default sheet
        createDefaultSheet();

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

        menuArchive.setName("文章归档");
        menuArchive.setUrl("/archives");
        menuArchive.setPriority(2);
        menuService.create(menuArchive.convertTo());

        MenuParam menuCategory = new MenuParam();
        menuCategory.setName("默认分类");
        menuCategory.setUrl("/categories/default");
        menuCategory.setPriority(3);
        menuService.create(menuCategory.convertTo());

        MenuParam menuSheet = new MenuParam();
        menuSheet.setName("关于页面");
        menuSheet.setUrl("/s/about");
        menuSheet.setPriority(4);
        menuService.create(menuSheet.convertTo());
    }


    @Nullable
    private void createDefaultComment(@Nullable PostDetailVO post) {
        if (post == null) {
            return;
        }

        long commentCount = postCommentService.count();

        if (commentCount > 0) {
            return;
        }

        PostComment comment = new PostComment();
        comment.setAuthor("Halo");
        comment.setAuthorUrl("https://halo.run");
        comment.setContent("欢迎使用 Halo，这是你的第一条评论，头像来自 [Gravatar](https://cn.gravatar.com)，你也可以通过注册 [Gravatar](https://cn.gravatar.com) 来显示自己的头像。");
        comment.setEmail("hi@halo.run");
        comment.setPostId(post.getId());
        postCommentService.create(comment);
    }

    @Nullable
    private PostDetailVO createDefaultPostIfAbsent(@Nullable Category category) {

        long publishedCount = postService.countByStatus(PostStatus.PUBLISHED);

        if (publishedCount > 0) {
            return null;
        }

        PostParam postParam = new PostParam();
        postParam.setSlug("hello-halo");
        postParam.setTitle("Hello Halo");
        postParam.setStatus(PostStatus.PUBLISHED);
        postParam.setOriginalContent("## Hello Halo\n" +
            "\n" +
            "如果你看到了这一篇文章，那么证明你已经安装成功了，感谢使用 [Halo](https://halo.run) 进行创作，希望能够使用愉快。\n" +
            "\n" +
            "## 相关链接\n" +
            "\n" +
            "- 官网：[https://halo.run](https://halo.run)\n" +
            "- 社区：[https://bbs.halo.run](https://bbs.halo.run)\n" +
            "- 主题仓库：[https://halo.run/s/themes](https://halo.run/s/themes)\n" +
            "- 开源地址：[https://github.com/halo-dev/halo](https://github.com/halo-dev/halo)\n" +
            "\n" +
            "在使用过程中，有任何问题都可以通过以上链接找寻答案，或者联系我们。\n" +
            "\n" +
            "> 这是一篇自动生成的文章，请删除这篇文章之后开始你的创作吧！\n" +
            "\n");

        Set<Integer> categoryIds = new HashSet<>();
        if (category != null) {
            categoryIds.add(category.getId());
            postParam.setCategoryIds(categoryIds);
        }
        return postService.createBy(postParam.convertTo(), Collections.emptySet(), categoryIds, false);
    }

    @Nullable
    private void createDefaultSheet() {
        long publishedCount = sheetService.countByStatus(PostStatus.PUBLISHED);
        if (publishedCount > 0) {
            return;
        }

        SheetParam sheetParam = new SheetParam();
        sheetParam.setSlug("about");
        sheetParam.setTitle("关于页面");
        sheetParam.setStatus(PostStatus.PUBLISHED);
        sheetParam.setOriginalContent("## 关于页面\n" +
            "\n" +
            "这是一个自定义页面，你可以在后台的 `页面` -> `所有页面` -> `自定义页面` 找到它，你可以用于新建关于页面、留言板页面等等。发挥你自己的想象力！\n" +
            "\n" +
            "> 这是一篇自动生成的页面，你可以在后台删除它。");
        sheetService.createBy(sheetParam.convertTo(), false);
    }

    @Nullable
    private Category createDefaultCategoryIfAbsent() {
        long categoryCount = categoryService.count();
        if (categoryCount > 0) {
            return null;
        }

        CategoryParam category = new CategoryParam();
        category.setName("默认分类");
        category.setSlug("default");
        category.setDescription("这是你的默认分类，如不需要，删除即可。");
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
            String gravatar = "//cn.gravatar.com/avatar/" + SecureUtil.md5(installParam.getEmail()) +
                "?s=256&d=mm";
            installParam.setAvatar(gravatar);
            return userService.createBy(installParam);
        });
    }

    private void initSettings(InstallParam installParam) {
        // Init default properties
        Map<PropertyEnum, String> properties = new HashMap<>(11);
        properties.put(PrimaryProperties.IS_INSTALLED, Boolean.TRUE.toString());
        properties.put(BlogProperties.BLOG_LOCALE, installParam.getLocale());
        properties.put(BlogProperties.BLOG_TITLE, installParam.getTitle());
        properties.put(BlogProperties.BLOG_URL, StringUtils.isBlank(installParam.getUrl()) ? optionService.getBlogBaseUrl() : installParam.getUrl());

        Long birthday = optionService.getByPropertyOrDefault(PrimaryProperties.BIRTHDAY, Long.class, 0L);

        if (birthday.equals(0L)) {
            properties.put(PrimaryProperties.BIRTHDAY, String.valueOf(System.currentTimeMillis()));
        }

        Boolean globalAbsolutePathEnabled = optionService.getByPropertyOrDefault(OtherProperties.GLOBAL_ABSOLUTE_PATH_ENABLED, Boolean.class, null);

        if (globalAbsolutePathEnabled == null) {
            properties.put(OtherProperties.GLOBAL_ABSOLUTE_PATH_ENABLED, Boolean.FALSE.toString());
        }

        // Create properties
        optionService.saveProperties(properties);
    }

}
