package run.halo.app.controller.admin.api;

import freemarker.template.Configuration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import run.halo.app.event.logger.LogEvent;
import run.halo.app.exception.BadRequestException;
import run.halo.app.model.entity.*;
import run.halo.app.model.enums.AttachmentType;
import run.halo.app.model.enums.LogType;
import run.halo.app.model.params.CategoryParam;
import run.halo.app.model.params.InstallParam;
import run.halo.app.model.properties.*;
import run.halo.app.model.support.BaseResponse;
import run.halo.app.service.*;
import run.halo.app.utils.ValidationUtils;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

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
    public BaseResponse<String> installBlog(@RequestBody @Valid InstallParam installParam) {
        // TODO Install blog.
        // Check is installed
        boolean isInstalled = Boolean.parseBoolean(optionService.getByProperty(PrimaryProperties.IS_INSTALLED).orElse(Boolean.FALSE.toString()));

        if (isInstalled) {
            // TODO i18n
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
        PostComment postComment = createDefaultComment();

        // Create default menu
        createDefaultMenu();

        // TODO Handle option cache

        // TODO i18n
        eventPublisher.publishEvent(
                new LogEvent(this, user.getId().toString(), LogType.BLOG_INITIALIZED, "博客已成功初始化")
        );

        // TODO i18n
        return BaseResponse.ok("安装完成！");
    }

    private void createDefaultMenu() {
        Menu menuIndex = new Menu();
        // TODO i18n
        menuIndex.setName("首页");
        menuIndex.setUrl("/");
        menuIndex.setPriority(1);
        menuService.create(menuIndex);

        Menu menuArchive = new Menu();
        // TODO i18n
        menuArchive.setName("归档");
        menuArchive.setUrl("/archives");
        menuArchive.setPriority(2);
        menuService.create(menuArchive);
    }


    private PostComment createDefaultComment() {
        // TODO Create default comment
        return null;
    }

    private Post createDefaultPost(Category category) {
        // TODO Create default post
        return null;
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
        properties.put(BlogProperties.BLOG_URL, installParam.getUrl());
        properties.put(PrimaryProperties.THEME, DEFAULT_THEME_ID);
        properties.put(PrimaryProperties.BIRTHDAY, String.valueOf(System.currentTimeMillis()));
        properties.put(EmailProperties.ENABLED, Boolean.FALSE.toString());
        properties.put(CommentProperties.NEW_NOTICE, Boolean.FALSE.toString());
        properties.put(CommentProperties.PASS_NOTICE, Boolean.FALSE.toString());
        properties.put(CommentProperties.REPLY_NOTICE, Boolean.FALSE.toString());
        properties.put(AttachmentProperties.ATTACHMENT_TYPE, AttachmentType.LOCAL.getValue().toString());

        // Create properties
        optionService.saveProperties(properties);
    }

}
