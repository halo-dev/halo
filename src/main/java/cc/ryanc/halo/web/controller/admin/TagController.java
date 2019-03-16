package cc.ryanc.halo.web.controller.admin;

import cc.ryanc.halo.service.TagService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Tags controller
 *
 * @author : RYAN0UP
 * @date : 2019-03-14
 */
@Controller
@RequestMapping(value = "/admin/tags")
public class TagController {

    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @GetMapping
    public String tags(){
        return "admin/admin_tag";
    }
}
