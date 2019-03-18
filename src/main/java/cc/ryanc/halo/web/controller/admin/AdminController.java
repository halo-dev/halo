package cc.ryanc.halo.web.controller.admin;

import cc.ryanc.halo.model.dto.LogOutputDTO;
import cc.ryanc.halo.model.dto.UserOutputDTO;
import cc.ryanc.halo.model.dto.post.PostSimpleOutputDTO;
import cc.ryanc.halo.model.entity.User;
import cc.ryanc.halo.model.vo.CommentVO;
import cc.ryanc.halo.service.*;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * Admin controller
 *
 * @author : RYAN0UP
 * @date : 2019-03-14
 */
@Controller
@RequestMapping(value = "/admin")
public class AdminController {

    private final PostService postService;

    private final CommentService commentService;

    private final AttachmentService attachmentService;

    private final LogService logService;

    private final UserService userService;

    public AdminController(PostService postService,
                           CommentService commentService,
                           AttachmentService attachmentService,
                           LogService logService,
                           UserService userService) {
        this.postService = postService;
        this.commentService = commentService;
        this.attachmentService = attachmentService;
        this.logService = logService;
        this.userService = userService;
    }

    /**
     * Admin dashboard.
     *
     * @param model model
     * @return template path: admin/admin_index.ftl
     */
    @GetMapping(value = {"", "index"})
    public String admin(Model model) {

        Page<PostSimpleOutputDTO> postPage = postService.listLatest(5);

        Page<CommentVO> commentPage = commentService.listLatest(5);

        Page<LogOutputDTO> logPage = logService.listLatest(5);

        model.addAttribute("postsCount", postPage.getTotalElements());
        model.addAttribute("commentsCount", commentPage.getTotalElements());
        model.addAttribute("attachmentsCount", attachmentService.count());

        model.addAttribute("latestPosts", postPage.getContent());
        model.addAttribute("latestLogs", logPage.getContent());
        model.addAttribute("latestComments", commentPage.getContent());

        return "admin/admin_index";
    }

    @PostMapping("login")
    @ResponseBody
    public UserOutputDTO login(@RequestParam("username") String username,
                               @RequestParam("password") String password) {
        // Login
        User user = userService.login(username, password);

        // Convert and return
        return new UserOutputDTO().convertFrom(user);
    }
}
