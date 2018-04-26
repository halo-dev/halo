package cc.ryanc.halo.web.controller.front;

import cc.ryanc.halo.model.domain.Category;
import cc.ryanc.halo.model.domain.Menu;
import cc.ryanc.halo.model.domain.Post;
import cc.ryanc.halo.model.domain.User;
import cc.ryanc.halo.model.dto.Archive;
import cc.ryanc.halo.model.dto.HaloConst;
import cc.ryanc.halo.service.CategoryService;
import cc.ryanc.halo.service.MenuService;
import cc.ryanc.halo.service.PostService;
import cc.ryanc.halo.service.UserService;
import cc.ryanc.halo.web.controller.core.BaseController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Date;
import java.util.List;

/**
 * @author : RYAN0UP
 * @version : 1.0
 * @date : 2018/4/26
 */
@Slf4j
@Controller
@RequestMapping(value = "/archives")
public class ArchivesController extends BaseController {

    @Autowired
    private PostService postService;

    @Autowired
    private UserService userService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private MenuService menuService;

    /**
     * 文章归档
     *
     * @param model model
     * @return 模板路径
     */
    @GetMapping
    public String archives(Model model){
        return this.archives(model,1);
    }

    /**
     * 文章归档分页
     *
     * @param model model
     * @param page page 当前页码
     * @return 模板路径/themes/{theme}/archives
     */
    @GetMapping(value = "page/{page}")
    public String archives(Model model,
                           @PathVariable(value = "page") Integer page){

        //所有文章数据，分页，material主题适用
        Sort sort = new Sort(Sort.Direction.DESC,"postDate");
        Pageable pageable = new PageRequest(page-1,5,sort);
        Page<Post> posts = postService.findPostByStatus(0,pageable);
        model.addAttribute("posts",posts);

        //文章总数
        model.addAttribute("postsCount",postService.findAllPosts().size());

        model.addAttribute("is_archives",true);

        //包含[List<Post>,year,month,count]
        List<Archive> archives = postService.findPostGroupByYearAndMonth();
        model.addAttribute("archives",archives);

        //包含[List<Post>,year,count]
        List<Archive> archivesLess = postService.findPostGroupByYear();
        model.addAttribute("archivesLess",archivesLess);

        //用户信息
        User user = userService.findUser();
        model.addAttribute("user",user);

        //菜单列表
        List<Menu> menus = menuService.findAllMenus();
        model.addAttribute("menus",menus);

        //所有分类目录
        List<Category> categories = categoryService.findAllCategories();
        model.addAttribute("categories",categories);

        //是否是归档页，用于判断输出链接
        model.addAttribute("isArchives","true");

        //设置选项
        model.addAttribute("options",HaloConst.OPTIONS);
        return this.render("archives");
    }

    /**
     * 文章归档，根据年月
     *
     * @param model model
     * @param year year 年份
     * @param month month 月份
     * @return 模板路径/themes/{theme}/archives
     */
    @GetMapping(value = "{year}/{month}")
    public String archives(Model model,
                           @PathVariable(value = "year") String year,
                           @PathVariable(value = "month") String month){
        log.info(year);
        log.info(month);

        //根据年月查出的文章数据，分页
        Sort sort = new Sort(Sort.Direction.DESC,"post_date");
        Pageable pageable = new PageRequest(0,5,sort);
        Page<Post> posts = postService.findPostByYearAndMonth(year,month,pageable);
        model.addAttribute("posts",posts);

        //文章总数
        model.addAttribute("postsCount",postService.findAllPosts().size());

        //用户信息
        User user = userService.findUser();
        model.addAttribute("user",user);

        //分类目录
        List<Category> categories = categoryService.findAllCategories();
        model.addAttribute("categories",categories);

        //菜单列表
        List<Menu> menus = menuService.findAllMenus();
        model.addAttribute("menus",menus);

        //归档数据，包含[year,month,count,List<Post>]
        List<Archive> archives = postService.findPostGroupByYearAndMonth();
        model.addAttribute("archives",archives);

        //是否是归档页，用于判断输出链接
        model.addAttribute("isArchives","true");

        //设置选项
        model.addAttribute("options",HaloConst.OPTIONS);
        return this.render("archives");
    }

    /**
     * 渲染文章详情
     *
     * @param postUrl 文章路径名
     * @param model model
     * @return 模板路径/themes/{theme}/post
     */
    @GetMapping(value = "{postUrl}")
    public String getPost(@PathVariable String postUrl, Model model){
        Post post = postService.findByPostUrl(postUrl);
        //获得当前文章的发布日期
        Date postDate = post.getPostDate();
        try {
            //查询当前文章日期之前的所有文章
            List<Post> beforePosts = postService.findByPostDateBefore(postDate);

            //查询当前文章日期之后的所有文章
            List<Post> afterPosts = postService.findByPostDateAfter(postDate);

            if(null!=beforePosts&&beforePosts.size()>0){
                model.addAttribute("beforePost",beforePosts.get(beforePosts.size()-1));
            }
            if(null!=afterPosts&&afterPosts.size()>0){
                model.addAttribute("afterPost",afterPosts.get(afterPosts.size()-1));
            }
        }catch (Exception e){
            log.error("未知错误：{0}",e.getMessage());
        }
        model.addAttribute("post",post);

        //文章总数
        model.addAttribute("postsCount",postService.findAllPosts().size());

        //用户信息
        User user = userService.findUser();
        model.addAttribute("user",user);

        //所有分类目录
        List<Category> categories = categoryService.findAllCategories();
        model.addAttribute("categories",categories);

        //归档数据，包含[year,month,count,List<Post>]
        List<Archive> archives = postService.findPostGroupByYearAndMonth();

        //菜单列表
        List<Menu> menus = menuService.findAllMenus();
        model.addAttribute("menus",menus);

        model.addAttribute("archives",archives);

        //设置选项
        model.addAttribute("options",HaloConst.OPTIONS);
        return this.render("post");
    }
}
