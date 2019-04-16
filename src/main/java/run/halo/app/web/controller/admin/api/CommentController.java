package run.halo.app.web.controller.admin.api;

import run.halo.app.model.dto.CommentOutputDTO;
import run.halo.app.model.entity.Comment;
import run.halo.app.model.entity.User;
import run.halo.app.model.enums.CommentStatus;
import run.halo.app.model.params.CommentParam;
import run.halo.app.model.properties.BlogProperties;
import run.halo.app.model.vo.CommentWithPostVO;
import run.halo.app.service.CommentService;
import run.halo.app.service.OptionService;
import run.halo.app.service.PostService;
import run.halo.app.utils.ValidationUtils;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import run.halo.app.model.dto.CommentOutputDTO;
import run.halo.app.model.entity.Comment;
import run.halo.app.model.entity.User;
import run.halo.app.model.enums.CommentStatus;
import run.halo.app.model.params.CommentParam;
import run.halo.app.model.properties.BlogProperties;
import run.halo.app.model.vo.CommentWithPostVO;
import run.halo.app.service.CommentService;
import run.halo.app.service.PostService;
import run.halo.app.utils.ValidationUtils;

import javax.servlet.http.HttpServletRequest;
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
    public List<CommentWithPostVO> pageLatest(@RequestParam(name = "top", defaultValue = "10") int top) {
        return commentService.pageLatest(top).getContent();
    }

    @GetMapping("status/{status}")
    public Page<CommentWithPostVO> pageBy(@PageableDefault(sort = "updateTime", direction = DESC) Pageable pageable,
                                          @PathVariable("status") CommentStatus status) {
        return commentService.pageBy(status, pageable);
    }

    @PostMapping
    public CommentOutputDTO createBy(@RequestBody CommentParam commentParam, HttpServletRequest request, User user) {
        // Set some default info
        commentParam.setAuthor(StringUtils.isEmpty(user.getNickname()) ? user.getUsername() : user.getNickname());
        commentParam.setEmail(user.getEmail());
        commentParam.setAuthorUrl(optionService.getByPropertyOfNullable(BlogProperties.BLOG_URL));

        // Validate the comment param manually
        ValidationUtils.validate(commentParam);

        // Check post id
        postService.mustExistById(commentParam.getPostId());

        // Check parent id
        if (commentParam.getParentId() != null && commentParam.getParentId() > 0) {
            commentService.mustExistById(commentParam.getParentId());
        }

        return new CommentOutputDTO().convertFrom(commentService.createBy(commentParam.convertTo(), request));
    }

    @PutMapping("{commentId:\\d+}/status/{status}")
    @ApiOperation("Updates comment status")
    public CommentOutputDTO updateStatusBy(@PathVariable("commentId") Long commentId,
                                           @PathVariable("status") CommentStatus status) {
        // Update comment status
        Comment updatedComment = commentService.updateStatus(commentId, status);

        return new CommentOutputDTO().convertFrom(updatedComment);
    }

    @DeleteMapping("{commentId:\\d+}")
    @ApiOperation("Deletes comment permanently and recursively")
    public CommentOutputDTO deleteBy(@PathVariable("commentId") Long commentId) {
        // Get comment by id
        Comment comment = commentService.getById(commentId);

        // Remove it
        commentService.remove(comment);

        return new CommentOutputDTO().convertFrom(comment);
    }
}
