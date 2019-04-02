package cc.ryanc.halo.web.controller.portal.api;

import cc.ryanc.halo.model.dto.post.PostDetailOutputDTO;
import cc.ryanc.halo.model.dto.post.PostSimpleOutputDTO;
import cc.ryanc.halo.model.enums.PostStatus;
import cc.ryanc.halo.service.PostService;
import io.swagger.annotations.ApiOperation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import static org.springframework.data.domain.Sort.Direction.DESC;

/**
 * Portal post controller.
 *
 * @author johnniang
 * @date 4/2/19
 */
@RestController("PortalPostController")
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping
    @ApiOperation("Lists posts")
    public Page<PostSimpleOutputDTO> pageBy(@PageableDefault(sort = "updateTime", direction = DESC) Pageable pageable) {
        return postService.pageSimpleDtoByStatus(PostStatus.PUBLISHED, pageable);
    }

    @GetMapping("{postId:\\d+}")
    @ApiOperation("Gets a post")
    public PostDetailOutputDTO getBy(@PathVariable("postId") Integer postId,
                                     @RequestParam(value = "formatDisabled", required = false, defaultValue = "true") Boolean formatDisabled,
                                     @RequestParam(value = "sourceDisabled", required = false, defaultValue = "false") Boolean sourceDisabled) {
        PostDetailOutputDTO postDetail = new PostDetailOutputDTO().convertFrom(postService.getById(postId));

        if (formatDisabled) {
            // Clear the format content
            postDetail.setFormatContent(null);
        }

        if (sourceDisabled) {
            // Clear the original content
            postDetail.setOriginalContent(null);
        }

        return postDetail;
    }
}
