package cc.ryanc.halo.web.controller.api;

import cc.ryanc.halo.model.domain.Post;
import cc.ryanc.halo.model.enums.PostTypeEnum;
import cc.ryanc.halo.model.params.JournalParam;
import cc.ryanc.halo.model.support.JsonResult;
import cc.ryanc.halo.service.PostService;
import cc.ryanc.halo.utils.MarkdownUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <pre>
 *     日志 API
 * </pre>
 *
 * @author : RYAN0UP
 * @date : 2019/03/04
 */
@RestController
@RequestMapping(value = "/api/journal")
public class ApiJournalController {

    @Autowired
    private PostService postService;

    /**
     * 发布日志
     *
     * @param journalParam journalParam
     * @return JsonResult
     */
    @PostMapping(value = "/save")
    public JsonResult save(JournalParam journalParam) {
        Post post = new Post();
        post.setPostContentMd(MarkdownUtils.renderMarkdown(journalParam.getContent()));
        post.setPostSource(journalParam.getSource());
        post.setPostType(PostTypeEnum.POST_TYPE_JOURNAL.getDesc());
        post = postService.create(post);
        return JsonResult.success("ok");
    }
}
