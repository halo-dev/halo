package cc.ryanc.halo.web.controller.front;

import cc.ryanc.halo.model.domain.*;
import cc.ryanc.halo.model.dto.Archive;
import cc.ryanc.halo.model.dto.HaloConst;
import cc.ryanc.halo.service.*;
import cc.ryanc.halo.web.controller.core.BaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * @author : RYAN0UP
 * @version : 1.0
 * @date : 2018/4/26
 */
@Controller
public class PagesController extends BaseController {

    @Autowired
    private GalleryService galleryService;

    @Autowired
    private UserService userService;

    @Autowired
    private PostService postService;

    @Autowired
    private MenuService menuService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private LinkService linkService;


    /**
     * 渲染关于页面
     *
     * @param model model
     * @return 模板路径/themes/{theme}/about
     */
    @GetMapping(value = "/about")
    public String about(Model model){
        model.addAttribute("about","709831589");

        //设置选项
        model.addAttribute("options",HaloConst.OPTIONS);
        return this.render("about");
    }

    /**
     * 跳转到图库页面
     *
     * @return 模板路径/themes/{theme}/gallery
     */
    @GetMapping(value = "/gallery")
    public String gallery(Model model){
        List<Gallery> galleries = galleryService.findAllGalleries();
        model.addAttribute("galleries",galleries);

        //用户信息
        User user = userService.findUser();
        model.addAttribute("user",user);

        model.addAttribute("is_gallery",true);

        //设置选项
        model.addAttribute("options",HaloConst.OPTIONS);
        return this.render("gallery");
    }

    /**
     * 友情链接
     *
     * @param model model
     * @return 模板路径/themes/{theme}/links
     */
    @GetMapping(value = "/links")
    public String links(Model model){

        //所有友情链接
        List<Link> links = linkService.findAllLinks();
        model.addAttribute("links",links);

        //用户信息
        User user = userService.findUser();
        model.addAttribute("user",user);

        model.addAttribute("is_links",true);

        //文章总数
        model.addAttribute("postsCount",postService.findAllPosts().size());

        //菜单列表
        List<Menu> menus = menuService.findAllMenus();
        model.addAttribute("menus",menus);

        //所有分类目录
        List<Category> categories = categoryService.findAllCategories();
        model.addAttribute("categories",categories);

        //归档数据，包含[year,month,count,List<Post>]
        List<Archive> archives = postService.findPostGroupByYearAndMonth();
        model.addAttribute("archives",archives);

        //设置选项
        model.addAttribute("options",HaloConst.OPTIONS);
        return this.render("links");
    }
}
