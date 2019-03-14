package cc.ryanc.halo.web.controller.admin;

import cc.ryanc.halo.model.dto.post.PostSimpleOutputDTO;
import cc.ryanc.halo.model.enums.PostStatus;
import cc.ryanc.halo.model.enums.PostType;
import cc.ryanc.halo.service.PostService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    /**
     * posts manage
     *
     * @param model    model
     * @param status   post status
     * @param pageable page info
     * @return template path: admin/admin_post.ftl
     */
    @GetMapping
    public String listPosts(Model model,
                            @PageableDefault(sort = {"topPriority", "createTime"}, direction = DESC) Pageable pageable,
                            @RequestParam(value = "status", defaultValue = "published") PostStatus status) {
        final Page<PostSimpleOutputDTO> postPage = postService.pageByStatusAndType(status, PostType.POST, pageable);

        model.addAttribute("posts", postPage.getContent());
        model.addAttribute("publishedCount", postService.countByStatusAndType(PostStatus.PUBLISHED, PostType.POST));
        model.addAttribute("draftCount", postService.countByStatusAndType(PostStatus.DRAFT, PostType.POST));
        model.addAttribute("recycleCount", postService.countByStatusAndType(PostStatus.RECYCLE, PostType.POST));
        model.addAttribute("status", status);
        return "admin/admin_post";
    }

}
