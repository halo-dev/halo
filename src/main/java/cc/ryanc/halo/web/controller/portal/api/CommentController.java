package cc.ryanc.halo.web.controller.portal.api;

import cc.ryanc.halo.model.dto.CommentOutputDTO;
import cc.ryanc.halo.model.entity.User;
import cc.ryanc.halo.model.params.CommentParam;
import cc.ryanc.halo.model.properties.BlogProperties;
import cc.ryanc.halo.security.authentication.Authentication;
import cc.ryanc.halo.security.context.SecurityContextHolder;
import cc.ryanc.halo.service.CommentService;
import cc.ryanc.halo.service.OptionService;
import cc.ryanc.halo.service.PostService;
import cc.ryanc.halo.utils.ValidationUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * Portal comment controller.
 *
 * @author johnniang
 * @date 4/3/19
 */
@RestController("PortalCommentController")
@RequestMapping("/api/comments")
public class CommentController {

    private final CommentService commentService;

    private final OptionService optionService;

    private final PostService postService;

    public CommentController(CommentService commentService,
                             OptionService optionService,
                             PostService postService) {
        this.commentService = commentService;
        this.optionService = optionService;
        this.postService = postService;
    }

    @PostMapping
    public CommentOutputDTO comment(@RequestBody CommentParam commentParam, HttpServletRequest request) {
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
}
