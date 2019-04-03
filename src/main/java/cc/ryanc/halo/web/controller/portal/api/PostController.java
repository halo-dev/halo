package cc.ryanc.halo.web.controller.portal.api;

import cc.ryanc.halo.model.dto.post.PostDetailOutputDTO;
import cc.ryanc.halo.model.dto.post.PostSimpleOutputDTO;
import cc.ryanc.halo.model.enums.PostStatus;
import cc.ryanc.halo.model.vo.CommentVO;
import cc.ryanc.halo.model.vo.CommentWithParentVO;
import cc.ryanc.halo.service.CommentService;
import cc.ryanc.halo.service.OptionService;
import cc.ryanc.halo.service.PostService;
import io.swagger.annotations.ApiOperation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
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

    private final CommentService commentService;

    private final OptionService optionService;

    public PostController(PostService postService,
                          CommentService commentService,
                          OptionService optionService) {
        this.postService = postService;
        this.commentService = commentService;
        this.optionService = optionService;
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

    @GetMapping("{postId:\\d+}/comments/tree_view")
    @ApiOperation("Lists comments with tree view")
    public Page<CommentVO> listCommentsTree(@PathVariable("postId") Integer postId,
                                            @RequestParam(name = "page", required = false, defaultValue = "0") int page,
                                            @SortDefault(sort = "createTime", direction = DESC) Sort sort) {
        return commentService.pageVosBy(postId, PageRequest.of(page, optionService.getCommentPageSize(), sort));
    }

    @GetMapping("{postId:\\d+}/comments/list_view")
    @ApiOperation("Lists comment with list view")
    public Page<CommentWithParentVO> listComments(@PathVariable("postId") Integer postId,
                                                  @RequestParam(name = "page", required = false, defaultValue = "0") int page,
                                                  @SortDefault(sort = "createTime", direction = DESC) Sort sort) {
        return commentService.pageWithParentVoBy(postId, PageRequest.of(page, optionService.getCommentPageSize(), sort));
    }
}
