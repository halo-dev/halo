package cc.ryanc.halo.web.controller;

import cc.ryanc.halo.model.domain.*;
import cc.ryanc.halo.model.dto.HaloConst;
import cc.ryanc.halo.model.dto.LogsRecord;
import cc.ryanc.halo.service.*;
import cc.ryanc.halo.util.HaloUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author : RYAN0UP
 * @date : 2018/1/28
 * @version : 1.0
 * description : 安装控制器
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

    /**
     * 渲染安装页面
     *
     * @return string
     */
    @GetMapping
    public String install(Model model){
        try{
            File basePath = new File(ResourceUtils.getURL("classpath:").getPath());
            File installFile = new File(basePath.getAbsolutePath(), "install.lock");
            if(installFile.exists()){
                model.addAttribute("isInstall",true);
            }else{
                model.addAttribute("isInstall",false);
            }
        }catch (Exception e){
            log.error(e.getMessage());
        }
        return "common/install";
    }

    /**
     * 执行安装
     *
     * @return boolean
     */
    @PostMapping(value = "/do")
    @ResponseBody
    public boolean doInstall(@RequestParam("siteTitle") String siteTitle,
                            @RequestParam("siteUrl") String siteUrl,
                            @RequestParam("userName") String userName,
                            @RequestParam("userDisplayName") String userDisplayName,
                            @RequestParam("userEmail") String userEmail,
                            @RequestParam("userPwd") String userPwd,
                            HttpServletRequest request){
        try{
            //创建install.lock文件
            File basePath = new File(ResourceUtils.getURL("classpath:").getPath());
            File installFile = new File(basePath.getAbsolutePath(), "install.lock");
            if(installFile.exists()){
                return false;
            }else{
                installFile.createNewFile();
            }
            //保存title设置
            optionsService.saveOption("site_title",siteTitle);
            optionsService.saveOption("site_url",siteUrl);

            //创建新的用户
            User user=  new User();
            user.setUserName(userName);
            if(StringUtils.isBlank(userDisplayName)){
                userDisplayName = userName;
            }
            user.setUserDisplayName(userDisplayName);
            user.setUserEmail(userEmail);
            user.setUserPass(HaloUtil.getMD5(userPwd));
            userService.saveByUser(user);

            //默认分类
            Category category = new Category();
            category.setCateName("未分类");
            category.setCateUrl("default");
            category.setCateDesc("未分类");
            categoryService.saveByCategory(category);

            //第一篇文章
            Post post = new Post();
            List<Category> categories = new ArrayList<>();
            categories.add(category);
            post.setPostTitle("Hello Halo!");
            post.setPostContentMd("#Hello Halo!\n" +
                    "欢迎使用Halo进行创作，删除这篇文章后赶紧开始吧。");
            post.setPostContent("<h1 id=\"h1-hello-halo-\"><a name=\"Hello Halo!\" class=\"reference-link\"></a><span class=\"header-link octicon octicon-link\"></span>Hello Halo!</h1><p>欢迎使用Halo进行创作，删除这篇文章后赶紧开始吧。</p>\n");
            post.setPostSummary("欢迎使用Halo进行创作，删除这篇文章后赶紧开始吧。");
            post.setPostStatus(0);
            post.setPostDate(new Date());
            post.setPostUrl("hello-halo");
            post.setUser(user);
            post.setCategories(categories);
            postService.saveByPost(post);

            //第一个评论
            Comment comment = new Comment();
            comment.setPost(post);
            comment.setCommentAuthor("ruibaby");
            comment.setCommentAuthorEmail("i@ryanc.cc");
            comment.setCommentAuthorUrl("https://ryanc.cc");
            comment.setCommentAuthorIp("127.0.0.1");
            comment.setCommentAuthorAvatarMd5("7cc7f29278071bd4dce995612d428834");
            comment.setCommentDate(new Date());
            comment.setCommentContent("欢迎，欢迎！");
            comment.setCommentAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.162 Safari/537.36");
            comment.setIsAdmin(0);
            commentService.saveByComment(comment);

            //设置默认主题
            optionsService.saveOption("theme","halo");

            //建立网站时间
            optionsService.saveOption("site_start",HaloUtil.getStringDate("yyyy-MM-dd"));

            //默认评论系统
            optionsService.saveOption("comment_system","native");

            //默认不配置邮件系统
            optionsService.saveOption("smtp_email_enable","false");

            //新评论，审核通过，回复，默认不通知
            optionsService.saveOption("new_comment_notice","false");
            optionsService.saveOption("comment_pass_notice","false");
            optionsService.saveOption("comment_reply_notice","false");

            //更新日志
            logsService.saveByLogs(
                    new Logs(
                            LogsRecord.INSTALL,
                            "安装成功，欢迎使用Halo。",
                            HaloUtil.getIpAddr(request),
                            HaloUtil.getDate()
                    )
            );

            HaloConst.OPTIONS.clear();
            HaloConst.OPTIONS = optionsService.findAllOptions();
        }catch (Exception e){
            log.error(e.getMessage());
            return false;
        }
        return true;
    }
}
