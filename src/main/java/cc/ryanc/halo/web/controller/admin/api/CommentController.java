package cc.ryanc.halo.web.controller.admin.api;

import cc.ryanc.halo.model.enums.CommentStatus;
import cc.ryanc.halo.model.vo.CommentVO;
import cc.ryanc.halo.service.CommentService;
import io.swagger.annotations.ApiOperation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

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

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping("latest")
    @ApiOperation("Pages latest comments")
    public List<CommentVO> pageLatest(@RequestParam(name = "top", defaultValue = "10") int top) {
        return commentService.pageLatest(top).getContent();
    }

    @GetMapping("status/{status}")
    public Page<CommentVO> pageBy(@PageableDefault(sort = "updateTime", direction = DESC) Pageable pageable,
                                  @PathVariable("status") CommentStatus status) {
        return commentService.pageBy(status, pageable);
    }
}
