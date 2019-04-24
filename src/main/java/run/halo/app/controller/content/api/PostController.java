package run.halo.app.controller.content.api;

import io.swagger.annotations.ApiOperation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.web.bind.annotation.*;
import run.halo.app.model.dto.post.PostDetailDTO;
import run.halo.app.model.dto.post.PostSimpleDTO;
import run.halo.app.model.enums.PostStatus;
import run.halo.app.model.vo.BaseCommentVO;
import run.halo.app.model.vo.BaseCommentWithParentVO;
import run.halo.app.service.CommentService;
import run.halo.app.service.OptionService;
import run.halo.app.service.PostService;

import static org.springframework.data.domain.Sort.Direction.DESC;

/**
 * Portal post controller.
 *
 * @author johnniang
 * @date 4/2/19
 */
@RestController("ApiContentPostController")
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
    public Page<PostSimpleDTO> pageBy(@PageableDefault(sort = "updateTime", direction = DESC) Pageable pageable) {
        return postService.pageSimpleDtoByStatus(PostStatus.PUBLISHED, pageable);
    }

    @GetMapping("{postId:\\d+}")
    @ApiOperation("Gets a post")
    public PostDetailDTO getBy(@PathVariable("postId") Integer postId,
                               @RequestParam(value = "formatDisabled", required = false, defaultValue = "true") Boolean formatDisabled,
                               @RequestParam(value = "sourceDisabled", required = false, defaultValue = "false") Boolean sourceDisabled) {
        PostDetailDTO postDetail = new PostDetailDTO().convertFrom(postService.getById(postId));

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
    public Page<BaseCommentVO> listCommentsTree(@PathVariable("postId") Integer postId,
                                                @RequestParam(name = "page", required = false, defaultValue = "0") int page,
                                                @SortDefault(sort = "createTime", direction = DESC) Sort sort) {
        return commentService.pageVosBy(postId, PageRequest.of(page, optionService.getCommentPageSize(), sort));
    }

    @GetMapping("{postId:\\d+}/comments/list_view")
    @ApiOperation("Lists comment with list view")
    public Page<BaseCommentWithParentVO> listComments(@PathVariable("postId") Integer postId,
                                                      @RequestParam(name = "page", required = false, defaultValue = "0") int page,
                                                      @SortDefault(sort = "createTime", direction = DESC) Sort sort) {
        return commentService.pageWithParentVoBy(postId, PageRequest.of(page, optionService.getCommentPageSize(), sort));
    }

    @PostMapping("{postId:\\d+}/likes")
    @ApiOperation("Likes a post")
    public void like(@PathVariable("postId") Integer postId) {
        postService.increaseLike(postId);
    }
}
