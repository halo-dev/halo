package cc.ryanc.halo.web.controller.front;

import cc.ryanc.halo.model.domain.Gallery;
import cc.ryanc.halo.model.domain.Link;
import cc.ryanc.halo.model.dto.Archive;
import cc.ryanc.halo.service.GalleryService;
import cc.ryanc.halo.service.LinkService;
import cc.ryanc.halo.service.PostService;
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
    private PostService postService;

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

        model.addAttribute("is_gallery",true);

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

        model.addAttribute("is_links",true);

        //归档数据，包含[year,month,count,List<Post>]
        List<Archive> archives = postService.findPostGroupByYearAndMonth();
        model.addAttribute("archives",archives);

        return this.render("links");
    }
}
