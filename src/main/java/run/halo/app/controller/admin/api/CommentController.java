package run.halo.app.controller.admin.api;

import io.swagger.annotations.ApiOperation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import run.halo.app.model.dto.CommentDTO;
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
    public CommentDTO createBy(@RequestBody CommentParam commentParam) {
        return new CommentDTO().convertFrom(commentService.createBy(commentParam));
    }

    @PutMapping("{commentId:\\d+}/status/{status}")
    @ApiOperation("Updates comment status")
    public CommentDTO updateStatusBy(@PathVariable("commentId") Long commentId,
                                     @PathVariable("status") CommentStatus status) {
        // Update comment status
        Comment updatedComment = commentService.updateStatus(commentId, status);

        return new CommentDTO().convertFrom(updatedComment);
    }

    @DeleteMapping("{commentId:\\d+}")
    @ApiOperation("Deletes comment permanently and recursively")
    public CommentDTO deleteBy(@PathVariable("commentId") Long commentId) {
        // Get comment by id
        Comment comment = commentService.getById(commentId);

        // Remove it
        commentService.remove(comment);

        return new CommentDTO().convertFrom(comment);
    }
}
