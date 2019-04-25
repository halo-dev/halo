package run.halo.app.controller.admin.api;

import io.swagger.annotations.ApiOperation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import run.halo.app.model.dto.BaseCommentDTO;
import run.halo.app.model.entity.Comment;
import run.halo.app.model.enums.CommentStatus;
import run.halo.app.model.params.CommentParam;
import run.halo.app.model.params.CommentQuery;
import run.halo.app.model.vo.CommentWithPostVO;
import run.halo.app.service.CommentService;

import java.util.List;

import static org.springframework.data.domain.Sort.Direction.DESC;

/**
 * Comment controller.
 *
 * @author johnniang
 * @date 3/19/19
 */
@RestController
@RequestMapping("/api/admin/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping
    @ApiOperation("Lists comments")
    public Page<CommentWithPostVO> pageBy(@PageableDefault(sort = "updateTime", direction = DESC) Pageable pageable,
                                          CommentQuery commentQuery) {
        Page<Comment> commentPage = commentService.pageBy(commentQuery, pageable);
        return commentService.convertToWithPostVo(commentPage);
    }

    @GetMapping("latest")
    @ApiOperation("Pages latest comments")
    public List<CommentWithPostVO> pageLatest(@RequestParam(name = "top", defaultValue = "10") int top) {
        List<Comment> content = commentService.pageLatest(top).getContent();
        return commentService.convertToWithPostVo(content);
    }

    @GetMapping("status/{status}")
    public Page<CommentWithPostVO> pageBy(@PageableDefault(sort = "updateTime", direction = DESC) Pageable pageable,
                                          @PathVariable("status") CommentStatus status) {
        Page<Comment> commentPage = commentService.pageBy(status, pageable);
        return commentService.convertToWithPostVo(commentPage);
    }

    @PostMapping
    @ApiOperation("Creates a comment (new or reply)")
    public BaseCommentDTO createBy(@RequestBody CommentParam commentParam) {
        Comment createdComment = commentService.createBy(commentParam);
        return commentService.convertTo(createdComment);
    }

    @PutMapping("{commentId:\\d+}/status/{status}")
    @ApiOperation("Updates comment status")
    public BaseCommentDTO updateStatusBy(@PathVariable("commentId") Long commentId,
                                         @PathVariable("status") CommentStatus status) {
        // Update comment status
        Comment updatedComment = commentService.updateStatus(commentId, status);

        return commentService.convertTo(updatedComment);
    }

    @DeleteMapping("{commentId:\\d+}")
    @ApiOperation("Deletes comment permanently and recursively")
    public BaseCommentDTO deleteBy(@PathVariable("commentId") Long commentId) {
        Comment deletedComment = commentService.removeById(commentId);

        return commentService.convertTo(deletedComment);
    }
}
