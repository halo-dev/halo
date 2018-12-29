package cc.ryanc.halo.web.controller.core;

import cc.ryanc.halo.model.domain.*;
import cc.ryanc.halo.model.dto.HaloConst;
import cc.ryanc.halo.model.dto.JsonResult;
import cc.ryanc.halo.model.dto.LogsRecord;
import cc.ryanc.halo.model.enums.*;
import cc.ryanc.halo.service.*;
import cc.ryanc.halo.utils.MarkdownUtils;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import freemarker.template.Configuration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <pre>
 *     博客初始化控制器
 * </pre>
 *
 * @author : RYAN0UP
 * @date : 2018/1/28
 */
@Slf4j
@Controller
@RequestMapping(value = "/install")
public class InstallController {

    @Autowired
    private OptionsService optionsService;

    @Autowired
    private UserService userService;

    @Autowired
    private LogsService logsService;

    @Autowired
    private PostService postService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private MenuService menuService;

    @Autowired
    private Configuration configuration;

    /**
     * 渲染安装页面
     *
     * @param model model
     *
     * @return 模板路径
     */
    @GetMapping
    public String install(Model model) {
        try {
            if (StrUtil.equals(TrueFalseEnum.TRUE.getDesc(), HaloConst.OPTIONS.get(BlogPropertiesEnum.IS_INSTALL.getProp()))) {
                model.addAttribute("isInstall", true);
            } else {
                model.addAttribute("isInstall", false);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return "common/install";
    }

    /**
     * 执行安装
     *
     * @param blogLocale      系统语言
     * @param siteTitle       博客标题
     * @param siteUrl         博客网址
     * @param userName        用户名
     * @param userDisplayName 用户名显示名
     * @param userEmail       用户邮箱
     * @param userPwd         用户密码
     * @param request         request
     *
     * @return JsonResult
     */
    @PostMapping(value = "/do")
    @ResponseBody
    public JsonResult doInstall(@RequestParam("blogLocale") String blogLocale,
                                @RequestParam("blogTitle") String blogTitle,
                                @RequestParam("blogUrl") String blogUrl,
                                @RequestParam("userName") String userName,
                                @RequestParam("userDisplayName") String userDisplayName,
                                @RequestParam("userEmail") String userEmail,
                                @RequestParam("userPwd") String userPwd,
                                HttpServletRequest request) {
        try {
            if (StrUtil.equals(TrueFalseEnum.TRUE.getDesc(), HaloConst.OPTIONS.get(BlogPropertiesEnum.IS_INSTALL.getProp()))) {
                return new JsonResult(ResultCodeEnum.FAIL.getCode(), "该博客已初始化，不能再次安装！");
            }
            //创建新的用户
            final User user = new User();
            user.setUserName(userName);
            if (StrUtil.isBlank(userDisplayName)) {
                userDisplayName = userName;
            }
            user.setUserDisplayName(userDisplayName);
            user.setUserEmail(userEmail);
            user.setUserPass(SecureUtil.md5(userPwd));
            userService.save(user);

            //默认分类
            final Category category = new Category();
            category.setCateName("未分类");
            category.setCateUrl("default");
            category.setCateDesc("未分类");
            categoryService.save(category);

            //第一篇文章
            final Post post = new Post();
            final List<Category> categories = new ArrayList<>();
            categories.add(category);
            post.setPostTitle("Hello Halo!");
            post.setPostContentMd("# Hello Halo!\n" +
                    "欢迎使用Halo进行创作，删除这篇文章后赶紧开始吧。");
            post.setPostContent(MarkdownUtils.renderMarkdown(post.getPostContentMd()));
            post.setPostSummary("欢迎使用Halo进行创作，删除这篇文章后赶紧开始吧。");
            post.setPostStatus(0);
            post.setPostDate(DateUtil.date());
            post.setPostUrl("hello-halo");
            post.setUser(user);
            post.setCategories(categories);
            post.setAllowComment(AllowCommentEnum.ALLOW.getCode());
            postService.save(post);

            //第一个评论
            final Comment comment = new Comment();
            comment.setPost(post);
            comment.setCommentAuthor("ruibaby");
            comment.setCommentAuthorEmail("i@ryanc.cc");
            comment.setCommentAuthorUrl("https://ryanc.cc");
            comment.setCommentAuthorIp("127.0.0.1");
            comment.setCommentAuthorAvatarMd5(SecureUtil.md5("i@ryanc.cc"));
            comment.setCommentDate(DateUtil.date());
            comment.setCommentContent("欢迎，欢迎！");
            comment.setCommentStatus(0);
            comment.setCommentAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.162 Safari/537.36");
            comment.setIsAdmin(0);
            commentService.save(comment);

            final Map<String, String> options = new HashMap<>();
            options.put(BlogPropertiesEnum.IS_INSTALL.getProp(), TrueFalseEnum.TRUE.getDesc());
            options.put(BlogPropertiesEnum.BLOG_LOCALE.getProp(), blogLocale);
            options.put(BlogPropertiesEnum.BLOG_TITLE.getProp(), blogTitle);
            options.put(BlogPropertiesEnum.BLOG_URL.getProp(), blogUrl);
            options.put(BlogPropertiesEnum.THEME.getProp(), "anatole");
            options.put(BlogPropertiesEnum.BLOG_START.getProp(), DateUtil.format(DateUtil.date(), "yyyy-MM-dd"));
            options.put(BlogPropertiesEnum.SMTP_EMAIL_ENABLE.getProp(), TrueFalseEnum.FALSE.getDesc());
            options.put(BlogPropertiesEnum.NEW_COMMENT_NOTICE.getProp(), TrueFalseEnum.FALSE.getDesc());
            options.put(BlogPropertiesEnum.COMMENT_PASS_NOTICE.getProp(), TrueFalseEnum.FALSE.getDesc());
            options.put(BlogPropertiesEnum.COMMENT_REPLY_NOTICE.getProp(), TrueFalseEnum.FALSE.getDesc());
            options.put(BlogPropertiesEnum.ATTACH_LOC.getProp(), AttachLocationEnum.SERVER.getDesc());
            optionsService.saveOptions(options);

            //更新日志
            logsService.save(LogsRecord.INSTALL, "安装成功，欢迎使用Halo。", request);

            final Menu menuIndex = new Menu();
            menuIndex.setMenuName("首页");
            menuIndex.setMenuUrl("/");
            menuIndex.setMenuSort(1);
            menuIndex.setMenuIcon(" ");
            menuService.save(menuIndex);

            final Menu menuArchive = new Menu();
            menuArchive.setMenuName("归档");
            menuArchive.setMenuUrl("/archives");
            menuArchive.setMenuSort(2);
            menuArchive.setMenuIcon(" ");
            menuService.save(menuArchive);

            HaloConst.OPTIONS.clear();
            HaloConst.OPTIONS = optionsService.findAllOptions();
            configuration.setSharedVariable("options", HaloConst.OPTIONS);
            configuration.setSharedVariable("user", userService.findUser());
        } catch (Exception e) {
            log.error(e.getMessage());
            return new JsonResult(ResultCodeEnum.FAIL.getCode(), e.getMessage());
        }
        return new JsonResult(ResultCodeEnum.SUCCESS.getCode(), "安装成功！");
    }
}
