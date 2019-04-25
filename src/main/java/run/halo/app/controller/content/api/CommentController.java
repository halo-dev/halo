package run.halo.app.controller.content.api;

import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import run.halo.app.model.dto.BaseCommentDTO;
import run.halo.app.model.params.CommentParam;
import run.halo.app.service.PostCommentService;

/**
 * Portal comment controller.
 *
 * @author johnniang
 * @date 4/3/19
 */
@RestController("ApiContentCommentController")
@RequestMapping("/api/comments")
public class CommentController {

    private final PostCommentService postCommentService;

    public CommentController(PostCommentService postCommentService) {
        this.postCommentService = postCommentService;
    }

    @PostMapping
    @ApiOperation("Comments a post")
    public BaseCommentDTO comment(@RequestBody CommentParam commentParam) {
        return postCommentService.convertTo(postCommentService.createBy(commentParam));
    }
}
