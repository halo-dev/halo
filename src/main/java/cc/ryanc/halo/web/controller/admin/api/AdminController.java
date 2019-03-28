package cc.ryanc.halo.web.controller.admin.api;

import cc.ryanc.halo.cache.lock.CacheLock;
import cc.ryanc.halo.model.dto.CountOutputDTO;
import cc.ryanc.halo.model.dto.UserOutputDTO;
import cc.ryanc.halo.model.enums.BlogProperties;
import cc.ryanc.halo.model.enums.PostStatus;
import cc.ryanc.halo.model.params.LoginParam;
import cc.ryanc.halo.service.*;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

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

    private final UserService userService;

    public AdminController(PostService postService,
                           AttachmentService attachmentService,
                           CommentService commentService,
                           OptionService optionService,
                           UserService userService) {
        this.postService = postService;
        this.attachmentService = attachmentService;
        this.commentService = commentService;
        this.optionService = optionService;
        this.userService = userService;
    }

    @GetMapping("counts")
    @ApiOperation("Gets count info")
    public CountOutputDTO getCount() {
        CountOutputDTO countOutputDTO = new CountOutputDTO();
        countOutputDTO.setPostCount(postService.countByStatus(PostStatus.PUBLISHED));
        countOutputDTO.setAttachmentCount(attachmentService.count());
        countOutputDTO.setCommentCount(commentService.count());
        countOutputDTO.setEstablishDays(Long.valueOf(optionService.getByProperty(BlogProperties.WIDGET_DAYCOUNT).orElse("0")));
        return countOutputDTO;
    }

    @PostMapping("login")
    @ApiOperation("Login with session")
    @CacheLock(autoDelete = false, traceRequest = true)
    public UserOutputDTO login(@Valid @RequestBody LoginParam loginParam, HttpServletRequest request) {
        return new UserOutputDTO().convertFrom(userService.login(loginParam.getUsername(), loginParam.getPassword(), request.getSession()));
    }
}
