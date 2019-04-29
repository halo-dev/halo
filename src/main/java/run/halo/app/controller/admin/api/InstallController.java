package run.halo.app.controller.admin.api;

import freemarker.template.Configuration;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import run.halo.app.cache.lock.CacheLock;
import run.halo.app.event.logger.LogEvent;
import run.halo.app.exception.BadRequestException;
import run.halo.app.model.entity.Category;
import run.halo.app.model.entity.Post;
import run.halo.app.model.entity.User;
import run.halo.app.model.enums.AttachmentType;
import run.halo.app.model.enums.LogType;
import run.halo.app.model.enums.PostStatus;
import run.halo.app.model.params.*;
import run.halo.app.model.properties.*;
import run.halo.app.model.support.BaseResponse;
import run.halo.app.service.*;
import run.halo.app.utils.ValidationUtils;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static run.halo.app.model.support.HaloConst.DEFAULT_THEME_ID;

/**
 * Installation controller.
 *
 * @author ryanwang
 * @date : 2019-03-17
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

    private final Configuration configuration;

    private final ApplicationEventPublisher eventPublisher;

    public InstallController(UserService userService,
                             CategoryService categoryService,
                             PostService postService,
                             PostCommentService postCommentService,
                             OptionService optionService,
                             MenuService menuService,
                             Configuration configuration,
                             ApplicationEventPublisher eventPublisher) {
        this.userService = userService;
        this.categoryService = categoryService;
        this.postService = postService;
        this.postCommentService = postCommentService;
        this.optionService = optionService;
        this.menuService = menuService;
        this.configuration = configuration;
        this.eventPublisher = eventPublisher;
    }

    @PostMapping
    @ResponseBody
    @CacheLock
    public BaseResponse<String> installBlog(@RequestBody @Valid InstallParam installParam) {
        // TODO Install blog.
        // Check is installed
        boolean isInstalled = optionService.getByPropertyOrDefault(PrimaryProperties.IS_INSTALLED, Boolean.class, false);

        if (isInstalled) {
            throw new BadRequestException("该博客已初始化，不能再次安装！");
        }

        // Initialize settings
        initSettings(installParam);

        // Create default user
        User user = createDefaultUser(installParam);

        // Create default category
        Category category = createDefaultCategory();

        // Create default post
        Post post = createDefaultPost(category);

        // Create default postComment
        createDefaultComment(post);

        // Create default menu
        createDefaultMenu();

        // TODO Handle option cache


        eventPublisher.publishEvent(
                new LogEvent(this, user.getId().toString(), LogType.BLOG_INITIALIZED, "博客已成功初始化")
        );


        return BaseResponse.ok("安装完成！");
    }

    private void createDefaultMenu() {
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


    private void createDefaultComment(Post post) {
        PostCommentParam commentParam = new PostCommentParam();
        commentParam.setAuthor("Halo Bot");
        commentParam.setAuthorUrl("https://github.com/halo-dev/halo");
        commentParam.setContent("欢迎使用 Halo，这是你的第一条评论。");
        commentParam.setEmail("i@ryanc.cc");
        commentParam.setPostId(post.getId());
        postCommentService.create(commentParam.convertTo());
    }

    private Post createDefaultPost(Category category) {
        PostParam postParam = new PostParam();
        postParam.setUrl("hello-halo");
        postParam.setTitle("Hello Halo");
        postParam.setStatus(PostStatus.PUBLISHED);
        postParam.setOriginalContent("## Hello Halo!\n" +
                "\n" +
                "感谢使用 [Halo](https://github.com/halo-dev/halo) 进行创作，请删除该文章开始吧！");
        Set<Integer> categoryIds = new HashSet<>();
        categoryIds.add(category.getId());
        postParam.setCategoryIds(categoryIds);
        return postService.create(postParam.convertTo());
    }

    @NonNull
    private Category createDefaultCategory() {
        CategoryParam category = new CategoryParam();
        category.setName("未分类");
        category.setSlugName("default");
        category.setDescription("未分类");
        ValidationUtils.validate(category);
        return categoryService.create(category.convertTo());
    }

    private User createDefaultUser(InstallParam installParam) {
        return userService.createBy(installParam);
    }

    private void initSettings(InstallParam installParam) {
        // Init default properties
        Map<PropertyEnum, String> properties = new HashMap<>(11);
        properties.put(PrimaryProperties.IS_INSTALLED, Boolean.TRUE.toString());
        properties.put(BlogProperties.BLOG_LOCALE, installParam.getLocale());
        properties.put(BlogProperties.BLOG_TITLE, installParam.getTitle());
        properties.put(BlogProperties.BLOG_URL, StringUtils.isBlank(installParam.getUrl()) ?
                optionService.getBlogBaseUrl() : installParam.getUrl());
        properties.put(PrimaryProperties.THEME, DEFAULT_THEME_ID);
        properties.put(PrimaryProperties.BIRTHDAY, String.valueOf(System.currentTimeMillis()));
        properties.put(EmailProperties.ENABLED, Boolean.FALSE.toString());
        properties.put(CommentProperties.NEW_NOTICE, Boolean.FALSE.toString());
        properties.put(CommentProperties.PASS_NOTICE, Boolean.FALSE.toString());
        properties.put(CommentProperties.REPLY_NOTICE, Boolean.FALSE.toString());
        properties.put(AttachmentProperties.ATTACHMENT_TYPE, AttachmentType.LOCAL.name());

        // Create properties
        optionService.saveProperties(properties);
    }

}
