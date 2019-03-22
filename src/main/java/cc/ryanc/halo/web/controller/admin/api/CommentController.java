package cc.ryanc.halo.web.controller.admin.api;

import cc.ryanc.halo.model.dto.CommentOutputDTO;
import cc.ryanc.halo.model.entity.Comment;
import cc.ryanc.halo.model.enums.BlogProperties;
import cc.ryanc.halo.model.enums.CommentStatus;
import cc.ryanc.halo.model.params.CommentParam;
import cc.ryanc.halo.model.support.HaloConst;
import cc.ryanc.halo.model.vo.CommentVO;
import cc.ryanc.halo.service.CommentService;
import cc.ryanc.halo.utils.HaloUtils;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.extra.servlet.ServletUtil;
import io.swagger.annotations.ApiOperation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.ServletRequestUtils;
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

    @PostMapping
    public CommentOutputDTO createBy(@Valid @RequestBody CommentParam commentParam, HttpServletRequest request) {
        Comment comment = commentParam.convertTo();

        // Set some default value
        comment.setGavatarMd5(SecureUtil.md5(comment.getEmail()));
        comment.setIpAddress(ServletUtil.getClientIP(request));


//        commentService.createBy(comment)
        return null;
    }
}
