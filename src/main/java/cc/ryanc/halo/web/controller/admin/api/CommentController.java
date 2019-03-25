package cc.ryanc.halo.web.controller.admin.api;

import cc.ryanc.halo.model.dto.CommentOutputDTO;
import cc.ryanc.halo.model.entity.Comment;
import cc.ryanc.halo.model.enums.CommentStatus;
import cc.ryanc.halo.model.params.CommentParam;
import cc.ryanc.halo.model.vo.CommentListVO;
import cc.ryanc.halo.service.CommentService;
import cc.ryanc.halo.service.OptionService;
import cc.ryanc.halo.service.PostService;
import io.swagger.annotations.ApiOperation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

import static org.springframework.data.domain.Sort.Direction.DESC;

/**
 * Comment controller.
 *
 * @author johnniang
 * @date 3/19/19
 */
@RestController
@RequestMapping("/admin/api/comments")
public class CommentController {

    private final CommentService commentService;

    private final PostService postService;

    private final OptionService optionService;

    public CommentController(CommentService commentService,
                             PostService postService,
                             OptionService optionService) {
        this.commentService = commentService;
        this.postService = postService;
        this.optionService = optionService;
    }

    @GetMapping("latest")
    @ApiOperation("Pages latest comments")
    public List<CommentListVO> pageLatest(@RequestParam(name = "top", defaultValue = "10") int top) {
        return commentService.pageLatest(top).getContent();
    }

    @GetMapping("status/{status}")
    public Page<CommentListVO> pageBy(@PageableDefault(sort = "updateTime", direction = DESC) Pageable pageable,
                                      @PathVariable("status") CommentStatus status) {
        return commentService.pageBy(status, pageable);
    }

    @PostMapping
    public CommentOutputDTO createBy(@Valid @RequestBody CommentParam commentParam, HttpServletRequest request) {
        // Check post id
        postService.mustExistById(commentParam.getPostId());

        // Check parent id
        if (commentParam.getParentId() != null && commentParam.getParentId() > 0) {
            commentService.mustExistById(commentParam.getParentId());
        }

        return new CommentOutputDTO().convertFrom(commentService.createBy(commentParam.convertTo(), request));
    }

    @PutMapping("{commentId:\\d+}/status/{status}")
    @ApiOperation("Update comment status")
    public CommentOutputDTO updateStatusBy(@PathVariable("commentId") Long commentId,
                                           @PathVariable("status") CommentStatus status) {
        // Update comment status
        Comment updatedComment = commentService.updateStatus(commentId, status);

        return new CommentOutputDTO().convertFrom(updatedComment);
    }

    @DeleteMapping("{commentId:\\d+}")
    public CommentOutputDTO deleteBy(@PathVariable("commentId") Long commentId) {
        // Get comment by id
        Comment comment = commentService.getById(commentId);

        // Remove it
        commentService.remove(comment);

        return new CommentOutputDTO().convertFrom(comment);
    }
}
