package run.halo.app.controller.admin.api;

import io.swagger.annotations.ApiOperation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.web.bind.annotation.*;
import run.halo.app.model.dto.BaseCommentDTO;
import run.halo.app.model.entity.PostComment;
import run.halo.app.model.enums.CommentStatus;
import run.halo.app.model.params.CommentQuery;
import run.halo.app.model.params.PostCommentParam;
import run.halo.app.model.vo.BaseCommentVO;
import run.halo.app.model.vo.BaseCommentWithParentVO;
import run.halo.app.model.vo.PostCommentWithPostVO;
import run.halo.app.service.OptionService;
import run.halo.app.service.PostCommentService;

import javax.validation.Valid;
import java.util.List;

import static org.springframework.data.domain.Sort.Direction.DESC;

/**
 * Post comment controller.
 *
 * @author johnniang
 * @author ryanwang
 * @date 2019-03-29
 */
@RestController
@RequestMapping("/api/admin/posts/comments")
public class PostCommentController {

    private final PostCommentService postCommentService;

    private final OptionService optionService;

    public PostCommentController(PostCommentService postCommentService,
            OptionService optionService) {
        this.postCommentService = postCommentService;
        this.optionService = optionService;
    }

    @GetMapping
    @ApiOperation("Lists post comments")
    public Page<PostCommentWithPostVO> pageBy(@PageableDefault(sort = "createTime", direction = DESC) Pageable pageable,
            CommentQuery commentQuery) {
        Page<PostComment> commentPage = postCommentService.pageBy(commentQuery, pageable);
        return postCommentService.convertToWithPostVo(commentPage);
    }

    @GetMapping("latest")
    @ApiOperation("Pages post latest comments")
    public List<PostCommentWithPostVO> listLatest(@RequestParam(name = "top", defaultValue = "10") int top,
            @RequestParam(name = "status", required = false) CommentStatus status) {
        // Get latest comment
        List<PostComment> content = postCommentService.pageLatest(top, status).getContent();

        // Convert and return
        return postCommentService.convertToWithPostVo(content);
    }

    @GetMapping("{postId:\\d+}/tree_view")
    @ApiOperation("Lists post comments with tree view")
    public Page<BaseCommentVO> listCommentTree(@PathVariable("postId") Integer postId,
            @RequestParam(name = "page", required = false, defaultValue = "0") int page,
            @SortDefault(sort = "createTime", direction = DESC) Sort sort) {
        return postCommentService.pageVosAllBy(postId, PageRequest.of(page, optionService.getCommentPageSize(), sort));
    }

    @GetMapping("{postId:\\d+}/list_view")
    @ApiOperation("Lists post comment with list view")
    public Page<BaseCommentWithParentVO> listComments(@PathVariable("postId") Integer postId,
            @RequestParam(name = "page", required = false, defaultValue = "0") int page,
            @SortDefault(sort = "createTime", direction = DESC) Sort sort) {
        return postCommentService.pageWithParentVoBy(postId, PageRequest.of(page, optionService.getCommentPageSize(), sort));
    }

    @PostMapping
    @ApiOperation("Creates a post comment (new or reply)")
    public BaseCommentDTO createBy(@RequestBody PostCommentParam postCommentParam) {
        PostComment createdPostComment = postCommentService.createBy(postCommentParam);
        return postCommentService.convertTo(createdPostComment);
    }

    @PutMapping("{commentId:\\d+}/status/{status}")
    @ApiOperation("Updates post comment status")
    public BaseCommentDTO updateStatusBy(@PathVariable("commentId") Long commentId,
            @PathVariable("status") CommentStatus status) {
        // Update comment status
        PostComment updatedPostComment = postCommentService.updateStatus(commentId, status);
        return postCommentService.convertTo(updatedPostComment);
    }

    @PutMapping("status/{status}")
    @ApiOperation("Updates post comment status in batch")
    public List<BaseCommentDTO> updateStatusInBatch(@PathVariable(name = "status") CommentStatus status,
            @RequestBody List<Long> ids) {
        List<PostComment> comments = postCommentService.updateStatusByIds(ids, status);
        return postCommentService.convertTo(comments);
    }

    @DeleteMapping("{commentId:\\d+}")
    @ApiOperation("Deletes post comment permanently and recursively")
    public BaseCommentDTO deletePermanently(@PathVariable("commentId") Long commentId) {
        PostComment deletedPostComment = postCommentService.removeById(commentId);
        return postCommentService.convertTo(deletedPostComment);
    }

    @DeleteMapping
    @ApiOperation("Delete post comments permanently in batch by id array")
    public List<PostComment> deletePermanentlyInBatch(@RequestBody List<Long> ids) {
        return postCommentService.removeByIds(ids);
    }

    @GetMapping("{commentId:\\d+}")
    @ApiOperation("Gets a post comment by comment id")
    public PostCommentWithPostVO getBy(@PathVariable("commentId") Long commentId) {
        PostComment comment = postCommentService.getById(commentId);
        return postCommentService.convertToWithPostVo(comment);
    }

    @PutMapping("{commentId:\\d+}")
    @ApiOperation("Updates a post comment")
    public BaseCommentDTO updateBy(@Valid @RequestBody PostCommentParam commentParam,
            @PathVariable("commentId") Long commentId) {
        PostComment commentToUpdate = postCommentService.getById(commentId);

        commentParam.update(commentToUpdate);

        return postCommentService.convertTo(postCommentService.update(commentToUpdate));
    }
}
