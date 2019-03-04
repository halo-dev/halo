package cc.ryanc.halo.web.controller.api;

import cc.ryanc.halo.model.domain.Post;
import cc.ryanc.halo.model.params.JournalParam;
import cc.ryanc.halo.service.PostService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

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

    public ApiJournalController(PostService postService) {
        this.postService = postService;
    }

    /**
     * 发布日志
     *
     * @param journalParam journalParam
     * @return JsonResult
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Post save(@RequestBody JournalParam journalParam) {
        // TODO need to validate token

        Post post = journalParam.convertTo();

        return postService.create(post);
    }
}
