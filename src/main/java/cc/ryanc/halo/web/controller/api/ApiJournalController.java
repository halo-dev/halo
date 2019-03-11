package cc.ryanc.halo.web.controller.api;

import cc.ryanc.halo.model.domain.Post;
import cc.ryanc.halo.model.domain.User;
import cc.ryanc.halo.model.dto.PostDetailOutputDTO;
import cc.ryanc.halo.model.params.JournalParam;
import cc.ryanc.halo.service.PostService;
import cc.ryanc.halo.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

import static cc.ryanc.halo.model.support.HaloConst.USER_SESSION_KEY;

/**
 * <pre>
 *     日志 API
 * </pre>
 *
 * @author : RYAN0UP
 * @date : 2019/03/04
 */
@RestController
@RequestMapping(value = "/api/journals")
public class ApiJournalController {

    private final PostService postService;

    private final UserService userService;

    public ApiJournalController(PostService postService,
                                UserService userService) {
        this.postService = postService;
        this.userService = userService;
    }

    /**
     * 发布日志
     *
     * @param journalParam journalParam
     * @return JsonResult
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PostDetailOutputDTO save(@RequestBody JournalParam journalParam, HttpSession session) {

        User user = userService.findUser();

        Post post = journalParam.convertTo();
        post.setUser(user);

        return new PostDetailOutputDTO().convertFrom(postService.create(post));
    }
}
