package cc.ryanc.halo.web.controller.core;

import cc.ryanc.halo.exception.BadRequestException;
import cc.ryanc.halo.model.entity.*;
import cc.ryanc.halo.model.enums.AttachmentType;
import cc.ryanc.halo.model.enums.BlogProperties;
import cc.ryanc.halo.model.params.InstallParam;
import cc.ryanc.halo.model.support.BaseResponse;
import cc.ryanc.halo.service.*;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import freemarker.template.Configuration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

import static cc.ryanc.halo.model.support.HaloConst.DEFAULT_THEME_NAME;

/**
 * Installation controller.
 *
 * @author : RYAN0UP
 * @date : 2019-03-17
 */
@Slf4j
@Controller
@RequestMapping("/installations")
public class InstallController {

    private final UserService userService;

    private final CategoryService categoryService;

    private final PostService postService;

    private final CommentService commentService;

    private final OptionService optionService;

    private final MenuService menuService;

    private final Configuration configuration;

    public InstallController(UserService userService,
                             CategoryService categoryService,
                             PostService postService,
                             CommentService commentService,
                             OptionService optionService,
                             MenuService menuService,
                             Configuration configuration) {
        this.userService = userService;
        this.categoryService = categoryService;
        this.postService = postService;
        this.commentService = commentService;
        this.optionService = optionService;
        this.menuService = menuService;
        this.configuration = configuration;
    }

    /**
     * Render install page
     *
     * @param model model
     * @return template path: common/install.ftl
     */
    @GetMapping
    public String install(Model model) {
        try {
            if (StrUtil.equals(Boolean.TRUE.toString(), optionService.getByProperty(BlogProperties.IS_INSTALL).orElse(Boolean.FALSE.toString()))) {
                model.addAttribute("isInstall", true);
            } else {
                model.addAttribute("isInstall", false);
            }
        } catch (Exception e) {
            log.error("Error occurred", e);
        }
        return "common/install";
    }

    @PostMapping
    @ResponseBody
    public BaseResponse<?> installBlog(@Valid InstallParam installParam) {
        // TODO Install blog.
        // Check is installed
        boolean isInstalled = Boolean.parseBoolean(optionService.getByProperty(BlogProperties.IS_INSTALL).orElse(Boolean.FALSE.toString()));

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

        // Create default comment
        Comment comment = createDefaultComment();

        // Create default menu
        createDefaultMenu();

        // TODO Handle option cache

        return BaseResponse.ok("安装完成！");
    }

    private void createDefaultMenu() {
        Menu menuIndex = new Menu();
        // TODO i18n
        menuIndex.setName("首页");
        menuIndex.setUrl("/");
        menuIndex.setSort(1);
        menuService.create(menuIndex);

        Menu menuArchive = new Menu();
        menuArchive.setName("归档");
        menuArchive.setUrl("/archives");
        menuArchive.setSort(2);
        menuService.create(menuArchive);
    }


    private Comment createDefaultComment() {
        // TODO Create default comment
        return null;
    }

    private Post createDefaultPost(Category category) {
        // TODO Create default post
        return null;
    }

    private Category createDefaultCategory() {
        Category category = new Category();

        // TODO Multi level category

        category.setName("未分类");
        category.setSlugName("default");
        category.setDescription("未分类");
        return categoryService.create(category);
    }

    private User createDefaultUser(InstallParam installParam) {
        return userService.createBy(installParam, installParam.getPassword());
    }

    private void initSettings(InstallParam installParam) {
        // Init properties
        Map<BlogProperties, String> properties = new HashMap<>(11);
        properties.put(BlogProperties.IS_INSTALL, Boolean.TRUE.toString());
        properties.put(BlogProperties.BLOG_LOCALE, installParam.getLocale());
        properties.put(BlogProperties.BLOG_TITLE, installParam.getTitle());
        properties.put(BlogProperties.BLOG_URL, installParam.getUrl());
        properties.put(BlogProperties.THEME, DEFAULT_THEME_NAME);
        properties.put(BlogProperties.BLOG_START, DateUtil.format(DateUtil.date(), "yyyy-MM-dd"));
        properties.put(BlogProperties.SMTP_EMAIL_ENABLE, Boolean.FALSE.toString());
        properties.put(BlogProperties.NEW_COMMENT_NOTICE, Boolean.FALSE.toString());
        properties.put(BlogProperties.COMMENT_PASS_NOTICE, Boolean.FALSE.toString());
        properties.put(BlogProperties.COMMENT_REPLY_NOTICE, Boolean.FALSE.toString());
        properties.put(BlogProperties.ATTACHMENT_TYPE, AttachmentType.SERVER.getValue().toString());

        // Create properties
        optionService.saveProperties(properties, "system");
    }

//    /**
//     * Do install
//     *
//     * @param blogLocale language
//     * @param blogTitle  blog title
//     * @param blogUrl    blog url
//     * @param userName   user name
//     * @param nickName   nick name
//     * @param userEmail  user email
//     * @param userPwd    user password
//     * @param request    request
//     * @return JsonResult
//     */
//    @PostMapping(value = "/do")
//    @ResponseBody
//    @Deprecated
//    public JsonResult doInstall(@RequestParam("blogLocale") String blogLocale,
//                                @RequestParam("blogTitle") String blogTitle,
//                                @RequestParam("blogUrl") String blogUrl,
//                                @RequestParam("userName") String userName,
//                                @RequestParam("userDisplayName") String nickName,
//                                @RequestParam("userEmail") String userEmail,
//                                @RequestParam("userPwd") String userPwd,
//                                HttpServletRequest request) {
//        try {
//            if (StrUtil.equals("true", OPTIONS.get("is_install"))) {
//                return new JsonResult(0, "该博客已初始化，不能再次安装！");
//            }
//            // Create new user
//            final User user = new User();
//            user.setUsername(userName);
//            user.setNickname(StrUtil.isBlank(nickName) ? userName : nickName);
//            user.setEmail(userEmail);
//            user.setPassword(SecureUtil.md5(userPwd));
//            userService.create(user);
//
//            //默认分类
//            Category category = new Category();
//            category.setName("未分类");
//            category.setSlugName("default");
//            category.setDescription("未分类");
//            category = categoryService.create(category);
//
//            //第一篇文章
//            final Post post = new Post();
//            final List<Category> categories = new ArrayList<>(1);
//            categories.add(category);
//            post.setTitle("Hello Halo!");
//            post.setOriginalContent("# Hello Halo!\n" +
//                    "欢迎使用Halo进行创作，删除这篇文章后赶紧开始吧。");
//            post.setFormatContent(MarkdownUtils.renderMarkdown(post.getOriginalContent()));
//            post.setSummary("欢迎使用Halo进行创作，删除这篇文章后赶紧开始吧。");
//            post.setStatus(PostStatus.PUBLISHED);
//            post.setUrl("hello-halo");
//            post.setDisallowComment(true);
//            post.setThumbnail("/static/halo-frontend/images/thumbnail/thumbnail-" + RandomUtil.randomInt(1, 11) + ".jpg");
//            postService.create(post);
//
//            //第一个评论
//            final Comment comment = new Comment();
//            comment.setAuthor("ruibaby");
//            comment.setEmail("i@ryanc.cc");
//            comment.setAuthorUrl("https://ryanc.cc");
//            comment.setIpAddress("127.0.0.1");
//            comment.setGavatarMd5(SecureUtil.md5("i@ryanc.cc"));
//            comment.setContent("欢迎，欢迎！");
//            comment.setStatus(CommentStatus.PUBLISHED);
//            comment.setUserAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.162 Safari/537.36");
//            comment.setIsAdmin(false);
//            commentService.create(comment);
//
//            final Map<BlogProperties, Object> options = new HashMap<>();
////            options.put(BlogProperties.IS_INSTALL, TrueFalseEnum.TRUE.getDesc());
////            options.put(BlogProperties.BLOG_LOCALE, blogLocale);
////            options.put(BlogProperties.BLOG_TITLE, blogTitle);
////            options.put(BlogProperties.BLOG_URL, blogUrl);
////            options.put(BlogProperties.THEME, "anatole");
////            options.put(BlogProperties.BLOG_START, DateUtil.format(DateUtil.date(), "yyyy-MM-dd"));
////            options.put(BlogProperties.SMTP_EMAIL_ENABLE, TrueFalseEnum.FALSE.getDesc());
////            options.put(BlogProperties.NEW_COMMENT_NOTICE, TrueFalseEnum.FALSE.getDesc());
////            options.put(BlogProperties.COMMENT_PASS_NOTICE, TrueFalseEnum.FALSE.getDesc());
////            options.put(BlogProperties.COMMENT_REPLY_NOTICE, TrueFalseEnum.FALSE.getDesc());
////            options.put(BlogProperties.ATTACHMENT_TYPE, AttachLocationEnum.SERVER.getDesc());
////            optionService.saveOptions(options);
//
//            //更新日志
////            logsService.save(LogsRecord.INSTALL, "安装成功，欢迎使用Halo。", request);
//
//            final Menu menuIndex = new Menu();
//            menuIndex.setName("首页");
//            menuIndex.setUrl("/");
//            menuIndex.setSort(1);
//            menuService.create(menuIndex);
//
//            final Menu menuArchive = new Menu();
//            menuArchive.setName("归档");
//            menuArchive.setUrl("/archives");
//            menuArchive.setSort(2);
//            menuService.create(menuArchive);
//
//            OPTIONS.clear();
//            OPTIONS.putAll(optionService.listOptions());
//            configuration.setSharedVariable("options", OPTIONS);
////            configuration.setSharedVariable("user", userService.findUser());
//        } catch (Exception e) {
//            log.error(e.getMessage());
//            return new JsonResult(0, e.getMessage());
//        }
//        return new JsonResult(1, "安装成功！");
//    }
}
