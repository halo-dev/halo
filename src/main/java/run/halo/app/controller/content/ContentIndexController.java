package run.halo.app.controller.content;

import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import run.halo.app.controller.content.model.PostModel;
import run.halo.app.model.entity.Post;
import run.halo.app.model.enums.PostPermalinkType;
import run.halo.app.service.OptionService;
import run.halo.app.service.PostService;

/**
 * Blog index page controller
 *
 * @author ryanwang
 * @date 2019-03-17
 */
@Slf4j  //private final Logger logger = LoggerFactory.getLogger(当前类名.class); 获取log对象
@Controller
@RequestMapping
public class ContentIndexController {

    private final PostService postService;

    private final OptionService optionService;

    private final PostModel postModel;
// 没明白是怎么注入的  这样注入和@有什么区别
    public ContentIndexController(PostService postService,
        OptionService optionService,
        PostModel postModel) {
        this.postService = postService;
        this.optionService = optionService;
        this.postModel = postModel;
    }


    /**
     * Render blog index
     *
     * @param p post id
     * @param model model
     * @return template path: themes/{theme}/index.ftl
     */
    @GetMapping
    public String index(Integer p, String token, Model model) {

        PostPermalinkType permalinkType = optionService.getPostPermalinkType();

        if (PostPermalinkType.ID.equals(permalinkType) && !Objects.isNull(p)) {
            Post post = postService.getById(p);
            return postModel.content(post, token, model);
        }

        return this.index(model, 1);
    }

    /**
     * Render blog index
     *
     * @param model model
     * @param page current page number
     * @return template path: themes/{theme}/index.ftl
     */
    @GetMapping(value = "page/{page}")
    public String index(Model model,
        @PathVariable(value = "page") Integer page) {
        String list = postModel.list(page, model);
        System.out.println(list);
        return list;
        // return postModel.list(page, model);
    }
}
