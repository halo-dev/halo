package cc.ryanc.halo.web.controller.admin;

import cc.ryanc.halo.model.dto.post.PostSimpleOutputDTO;
import cc.ryanc.halo.model.enums.PostStatus;
import cc.ryanc.halo.model.enums.PostType;
import cc.ryanc.halo.service.PostService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import static org.springframework.data.domain.Sort.Direction.DESC;

/**
 * Posts controller
 *
 * @author : RYAN0UP
 * @date : 2019-03-14
 */
@Controller
@RequestMapping(value = "/admin/posts")
public class PostController {

    private PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    /**
     * posts manage
     *
     * @param model  model
     * @param status post status
     * @param page   current page
     * @param sort   sort
     *
     * @return template path: admin/admin_post.ftl
     */
    @GetMapping
    public String posts(Model model,
                        @RequestParam(value = "status", defaultValue = "0") PostStatus status,
                        @RequestParam(value = "page", defaultValue = "0") Integer page,
                        @SortDefault.SortDefaults({
                                @SortDefault(sort = "postPriority", direction = DESC),
                                @SortDefault(sort = "postDate", direction = DESC)
                        }) Sort sort) {
        final Pageable pageable = PageRequest.of(page, 10, sort);
        final Page<PostSimpleOutputDTO> posts = postService.listByStatus(status, PostType.POST, pageable);
        model.addAttribute("posts", posts);
        model.addAttribute("publishCount", postService.countByStatus(PostStatus.PUBLISHED, PostType.POST));
        model.addAttribute("draftCount", postService.countByStatus(PostStatus.DRAFT, PostType.POST));
        model.addAttribute("trashCount", postService.countByStatus(PostStatus.RECYCLE, PostType.POST));
        model.addAttribute("status", status);
        return "admin/admin_post";
    }
}
