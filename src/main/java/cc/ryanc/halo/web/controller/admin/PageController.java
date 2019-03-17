package cc.ryanc.halo.web.controller.admin;

import cc.ryanc.halo.service.PostService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Pages controller
 *
 * @author : RYAN0UP
 * @date : 2019-03-14
 */
@Controller
@RequestMapping(value = "/admin/pages")
public class PageController {

    private final PostService postService;

    public PageController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping
    public String pages(){
        return "admin/admin_page";
    }
}
