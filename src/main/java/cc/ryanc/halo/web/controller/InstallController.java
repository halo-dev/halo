package cc.ryanc.halo.web.controller;

import cc.ryanc.halo.model.domain.Category;
import cc.ryanc.halo.model.domain.Logs;
import cc.ryanc.halo.model.domain.Post;
import cc.ryanc.halo.model.domain.User;
import cc.ryanc.halo.model.dto.HaloConst;
import cc.ryanc.halo.model.dto.LogsRecord;
import cc.ryanc.halo.service.*;
import cc.ryanc.halo.util.HaloUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author : RYAN0UP
 * @version : 1.0
 * @date : 2018/1/28
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

    /**
     * 渲染安装页面
     *
     * @return string
     */
    @GetMapping
    public String install(){
        return "common/install";
    }

    /**
     * 执行安装
     *
     * @return string
     */
    @PostMapping(value = "/do")
    @ResponseBody
    public boolean doInstall(@RequestParam("siteTitle") String siteTitle,
                            @RequestParam("userName") String userName,
                            @RequestParam("userDisplayName") String userDisplayName,
                            @RequestParam("userEmail") String userEmail,
                            @RequestParam("userPwd") String userPwd,
                            HttpServletRequest request){

        //创建install.lock文件
        try{
            File basePath = new File(ResourceUtils.getURL("classpath:").getPath());
            File installFile = new File(basePath.getAbsolutePath(), "install.lock");
            System.out.println(installFile.getAbsolutePath());
            installFile.createNewFile();

            //保存title设置
            optionsService.saveOption("site_title",siteTitle);

            //创建新的用户
            User user=  new User();
            user.setUserName(userName);
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

            //设置默认主题
            optionsService.saveOption("theme","halo");

            //建立网站时间
            optionsService.saveOption("site_start",HaloUtil.getStringDate("yyyy-MM-dd"));

            //更新日志
            logsService.saveByLogs(
                    new Logs(
                            LogsRecord.INSTALL,
                            "欢迎使用Halo",
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
