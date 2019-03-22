package cc.ryanc.halo.web.controller.content;

import cc.ryanc.halo.model.entity.Tag;
import cc.ryanc.halo.model.enums.BlogProperties;
import cc.ryanc.halo.model.vo.PostListVO;
import cc.ryanc.halo.service.PostService;
import cc.ryanc.halo.service.TagService;
import cc.ryanc.halo.utils.HaloUtils;
import cc.ryanc.halo.web.controller.content.base.BaseContentController;
import cn.hutool.core.util.StrUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import static cc.ryanc.halo.model.support.HaloConst.OPTIONS;
import static org.springframework.data.domain.Sort.Direction.DESC;

/**
 * Tag Controller
 *
 * @author : RYAN0UP
 * @date : 2019-03-21
 */
@Controller
@RequestMapping(value = "/tags")
public class ContentTagController extends BaseContentController {

    private final TagService tagService;

    private final PostService postService;

    public ContentTagController(TagService tagService, PostService postService) {
        this.tagService = tagService;
        this.postService = postService;
    }

    /**
     * All of tags
     *
     * @return template path: themes/{theme}/tags
     */
    @GetMapping
    public String tags() {
        return this.render("tags");
    }

    /**
     * List tags by tag slug
     *
     * @param model    model
     * @param slugName slug name
     * @return template path: themes/{theme}/tag
     */
    @GetMapping(value = "{slugName}")
    public String tags(Model model,
                       @PathVariable("slugName") String slugName) {
        return this.tags(model, slugName, 1, Sort.by(DESC, "postDate"));
    }

    /**
     * List tags by tag slug
     *
     * @param model    model
     * @param slugName slug name
     * @param page     current page
     * @return template path: themes/{theme}/tag
     */
    @GetMapping(value = "{slugName}/page/{page}")
    public String tags(Model model,
                       @PathVariable("slugName") String slugName,
                       @PathVariable("page") Integer page,
                       @SortDefault(sort = "postDate", direction = DESC) Sort sort) {
        final Tag tag = tagService.getBySlugName(slugName);
        if (null == tag) {
            return this.renderNotFound();
        }
        int size = HaloUtils.getDefaultPageSize(10);
        final Pageable pageable = PageRequest.of(page - 1, size, sort);

        // TODO get posts by tag
        final Page<PostListVO> posts;
        //final int[] rainbow = PageUtil.rainbow(page, posts.getTotalPages(), 3);
//        model.addAttribute("is_tags", true);
//        model.addAttribute("posts", posts);
//        model.addAttribute("rainbow", rainbow);
//        model.addAttribute("tag", tag);
        return this.render("tag");
    }
}
