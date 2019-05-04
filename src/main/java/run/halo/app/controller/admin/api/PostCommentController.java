package run.halo.app.controller.admin.api;

import io.swagger.annotations.ApiOperation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import run.halo.app.model.dto.BaseCommentDTO;
import run.halo.app.model.entity.PostComment;
import run.halo.app.model.enums.CommentStatus;
import run.halo.app.model.params.CommentQuery;
import run.halo.app.model.params.PostCommentParam;
import run.halo.app.model.vo.PostCommentWithPostVO;
import run.halo.app.service.PostCommentService;

import java.util.List;

import static org.springframework.data.domain.Sort.Direction.DESC;

/**
 * Post comment controller.
 *
 * @author johnniang
 * @date 3/19/19
 */
@RestController
@RequestMapping("/api/admin/posts/comments")
public class PostCommentController {

    private final PostCommentService postCommentService;

    public PostCommentController(PostCommentService postCommentService) {
        this.postCommentService = postCommentService;
    }

    @GetMapping
    @ApiOperation("Lists post comments")
    public Page<PostCommentWithPostVO> pageBy(@PageableDefault(sort = "updateTime", direction = DESC) Pageable pageable,
                                              CommentQuery commentQuery) {
        Page<PostComment> commentPage = postCommentService.pageBy(commentQuery, pageable);
        return postCommentService.convertToWithPostVo(commentPage);
    }

    @GetMapping("latest")
    @ApiOperation("Pages latest comments")
    public List<PostCommentWithPostVO> pageLatest(@RequestParam(name = "top", defaultValue = "10") int top,
                                                  @RequestParam(name = "status", required = false) CommentStatus status) {
        // Get latest comment
        List<PostComment> content = postCommentService.pageLatest(top, status).getContent();

        // Convert and return
        return postCommentService.convertToWithPostVo(content);
    }

    @PostMapping
    @ApiOperation("Creates a comment (new or reply)")
    public BaseCommentDTO createBy(@RequestBody PostCommentParam postCommentParam) {
        PostComment createdPostComment = postCommentService.createBy(postCommentParam);
        return postCommentService.convertTo(createdPostComment);
    }

    @PutMapping("{commentId:\\d+}/status/{status}")
    @ApiOperation("Updates comment status")
    public BaseCommentDTO updateStatusBy(@PathVariable("commentId") Long commentId,
                                         @PathVariable("status") CommentStatus status) {
        // Update comment status
        PostComment updatedPostComment = postCommentService.updateStatus(commentId, status);
        return postCommentService.convertTo(updatedPostComment);
    }

    @DeleteMapping("{commentId:\\d+}")
    @ApiOperation("Deletes comment permanently and recursively")
    public BaseCommentDTO deleteBy(@PathVariable("commentId") Long commentId) {
        PostComment deletedPostComment = postCommentService.removeById(commentId);
        return postCommentService.convertTo(deletedPostComment);
    }
}
