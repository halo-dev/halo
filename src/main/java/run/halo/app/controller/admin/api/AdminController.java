package run.halo.app.controller.admin.api;

import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import run.halo.app.cache.lock.CacheLock;
import run.halo.app.exception.BadRequestException;
import run.halo.app.model.dto.CountDTO;
import run.halo.app.model.dto.UserDTO;
import run.halo.app.model.enums.PostStatus;
import run.halo.app.model.params.LoginParam;
import run.halo.app.model.properties.PrimaryProperties;
import run.halo.app.security.context.SecurityContextHolder;
import run.halo.app.security.filter.AdminAuthenticationFilter;
import run.halo.app.service.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * Admin controller.
 *
 * @author johnniang
 * @date 3/19/19
 */
@Slf4j
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final PostService postService;

    private final AttachmentService attachmentService;

    private final CommentService commentService;

    private final OptionService optionService;

    private final UserService userService;

    private final LinkService linkService;

    public AdminController(PostService postService,
                           AttachmentService attachmentService,
                           CommentService commentService,
                           OptionService optionService,
                           UserService userService,
                           LinkService linkService) {
        this.postService = postService;
        this.attachmentService = attachmentService;
        this.commentService = commentService;
        this.optionService = optionService;
        this.userService = userService;
        this.linkService = linkService;
    }

    /**
     * Get some statistics about the count of posts, the count of comments, etc.
     *
     * @return counts
     */
    @GetMapping("counts")
    @ApiOperation("Gets count info")
    public CountDTO getCount() {
        CountDTO countDTO = new CountDTO();
        countDTO.setPostCount(postService.countByStatus(PostStatus.PUBLISHED));
        countDTO.setAttachmentCount(attachmentService.count());
        countDTO.setCommentCount(commentService.count());

        long currentTimeMillis = System.currentTimeMillis();

        // Calculate birthday
        // TODO Initialize the birthday if absent
        Long birthday = optionService.getByPropertyOrDefault(PrimaryProperties.BIRTHDAY, Long.class, currentTimeMillis);
        long days = (currentTimeMillis - birthday) / (1000 * 24 * 3600);
        countDTO.setEstablishDays(days);

        countDTO.setLinkCount(linkService.count());
        countDTO.setVisitCount(postService.countVisit());
        countDTO.setLikeCount(postService.countLike());
        return countDTO;
    }

    @PostMapping("login")
    @ApiOperation("Login with session")
    @CacheLock(autoDelete = false, traceRequest = true)
    public UserDTO login(@Valid @RequestBody LoginParam loginParam, HttpServletRequest request) {
        return new UserDTO().convertFrom(userService.login(loginParam.getUsername(), loginParam.getPassword(), request.getSession()));
    }

    @PostMapping("logout")
    @ApiOperation("Logs out (Clear session)")
    @CacheLock
    public void logout(HttpServletRequest request) {
        // Check if the current is logging in
        boolean authenticated = SecurityContextHolder.getContext().isAuthenticated();

        if (!authenticated) {
            throw new BadRequestException("You haven't logged in yet, so you can't log out");
        }

        request.getSession().removeAttribute(AdminAuthenticationFilter.ADMIN_SESSION_KEY);

        log.info("You have been logged out, Welcome to you next time!");
    }
}
