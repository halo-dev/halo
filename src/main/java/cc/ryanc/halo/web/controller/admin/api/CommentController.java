package cc.ryanc.halo.web.controller.admin.api;

import cc.ryanc.halo.model.dto.CommentOutputDTO;
import cc.ryanc.halo.model.entity.Comment;
import cc.ryanc.halo.model.entity.User;
import cc.ryanc.halo.model.enums.BlogProperties;
import cc.ryanc.halo.model.enums.CommentStatus;
import cc.ryanc.halo.model.params.CommentParam;
import cc.ryanc.halo.model.vo.CommentWithPostVO;
import cc.ryanc.halo.security.authentication.Authentication;
import cc.ryanc.halo.security.context.SecurityContextHolder;
import cc.ryanc.halo.service.CommentService;
import cc.ryanc.halo.service.OptionService;
import cc.ryanc.halo.service.PostService;
import cc.ryanc.halo.utils.ValidationUtils;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

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
    public List<CommentWithPostVO> pageLatest(@RequestParam(name = "top", defaultValue = "10") int top) {
        return commentService.pageLatest(top).getContent();
    }

    @GetMapping("status/{status}")
    public Page<CommentWithPostVO> pageBy(@PageableDefault(sort = "updateTime", direction = DESC) Pageable pageable,
                                          @PathVariable("status") CommentStatus status) {
        return commentService.pageBy(status, pageable);
    }

    @PostMapping
    public CommentOutputDTO createBy(@RequestBody CommentParam commentParam, HttpServletRequest request) {
        // Get authentication
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            User user = authentication.getDetail().getUser();
            // If the admin is login
            commentParam.setAuthor(StringUtils.isEmpty(user.getNickname()) ? user.getUsername() : user.getNickname());
            commentParam.setEmail(user.getEmail());
            commentParam.setAuthorUrl(optionService.getByPropertyOfNullable(BlogProperties.BLOG_URL));
        }

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
