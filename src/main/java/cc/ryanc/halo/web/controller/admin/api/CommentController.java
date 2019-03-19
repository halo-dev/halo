package cc.ryanc.halo.web.controller.admin.api;

import cc.ryanc.halo.model.vo.CommentVO;
import cc.ryanc.halo.service.CommentService;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping("latest")
    @ApiOperation("Pages latest comments")
    public List<CommentVO> pageLatest(@RequestParam(name = "top", defaultValue = "10") int top) {
        return commentService.pageLatest(top).getContent();
    }
}
