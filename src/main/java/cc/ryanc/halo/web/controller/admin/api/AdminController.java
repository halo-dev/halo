package cc.ryanc.halo.web.controller.admin.api;

import cc.ryanc.halo.model.dto.CountOutputDTO;
import cc.ryanc.halo.model.enums.BlogProperties;
import cc.ryanc.halo.model.enums.PostStatus;
import cc.ryanc.halo.model.enums.PostType;
import cc.ryanc.halo.service.AttachmentService;
import cc.ryanc.halo.service.CommentService;
import cc.ryanc.halo.service.OptionService;
import cc.ryanc.halo.service.PostService;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin controller.
 *
 * @author johnniang
 * @date 3/19/19
 */
@RestController
@RequestMapping("/admin/api")
public class AdminController {

    private final PostService postService;

    private final AttachmentService attachmentService;

    private final CommentService commentService;

    private final OptionService optionService;

    public AdminController(PostService postService, AttachmentService attachmentService, CommentService commentService, OptionService optionService) {
        this.postService = postService;
        this.attachmentService = attachmentService;
        this.commentService = commentService;
        this.optionService = optionService;
    }

    @GetMapping("counts")
    @ApiOperation("Gets count info")
    public CountOutputDTO getCount() {
        CountOutputDTO countOutputDTO = new CountOutputDTO();
        countOutputDTO.setPostCount(postService.countByStatus(PostStatus.PUBLISHED, PostType.POST));
        countOutputDTO.setAttachmentCount(attachmentService.count());
        countOutputDTO.setCommentCount(commentService.count());
        countOutputDTO.setEstablishDays(Long.valueOf(optionService.getByProperty(BlogProperties.WIDGET_DAYCOUNT).orElse("0")));
        return countOutputDTO;
    }
}
