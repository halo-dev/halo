package run.halo.app.controller.content;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.PageUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import run.halo.app.cache.StringCacheStore;
import run.halo.app.cache.lock.CacheLock;
import run.halo.app.controller.content.model.PostModel;
import run.halo.app.model.entity.Post;
import run.halo.app.model.enums.PostStatus;
import run.halo.app.model.vo.PostListVO;
import run.halo.app.service.*;

import java.util.concurrent.TimeUnit;

import static org.springframework.data.domain.Sort.Direction.DESC;

/**
 * Blog archive page controller
 *
 * @author ryanwang
 * @author guqing
 * @author evanwang
 * @date 2019-03-17
 */
@Slf4j
@Controller
@RequestMapping(value = "/archives")
public class ContentArchiveController {

    private final PostService postService;

    private final ThemeService themeService;

    private final PostCategoryService postCategoryService;

    private final PostMetaService postMetaService;

    private final PostTagService postTagService;

    private final OptionService optionService;

    private final StringCacheStore cacheStore;

    private final PostModel postModel;

    public ContentArchiveController(PostService postService,
                                    ThemeService themeService,
                                    PostCategoryService postCategoryService,
                                    PostMetaService postMetaService,
                                    PostTagService postTagService,
                                    OptionService optionService,
                                    StringCacheStore cacheStore,
                                    PostModel postModel) {
        this.postService = postService;
        this.themeService = themeService;
        this.postCategoryService = postCategoryService;
        this.postMetaService = postMetaService;
        this.postTagService = postTagService;
        this.optionService = optionService;
        this.cacheStore = cacheStore;
        this.postModel = postModel;
    }

    /**
     * Render post archives page.
     *
     * @param model model
     * @return template path : themes/{theme}/archives.ftl
     */
    @GetMapping
    public String archives(Model model) {
        return this.archives(model, 1, Sort.by(DESC, "createTime"));
    }

    /**
     * Render post archives page.
     *
     * @param model model
     * @return template path : themes/{theme}/archives.ftl
     */
    @GetMapping(value = "page/{page}")
    public String archives(Model model,
                           @PathVariable(value = "page") Integer page,
                           @SortDefault(sort = "createTime", direction = DESC) Sort sort) {
        Pageable pageable = PageRequest.of(page - 1, optionService.getPostPageSize(), sort);

        Page<Post> postPage = postService.pageBy(PostStatus.PUBLISHED, pageable);
        Page<PostListVO> postListVos = postService.convertToListVo(postPage);
        int[] pageRainbow = PageUtil.rainbow(page, postListVos.getTotalPages(), 3);

        model.addAttribute("is_archives", true);
        model.addAttribute("pageRainbow", pageRainbow);
        model.addAttribute("posts", postListVos);

        return themeService.render("archives");
    }

    /**
     * Render post page.
     *
     * @param url   post slug url.
     * @param token view token.
     * @param model model
     * @return template path: themes/{theme}/post.ftl
     */
    @GetMapping("{url}")
    public String post(@PathVariable("url") String url,
                       @RequestParam(value = "token", required = false) String token,
                       Model model) {
        Post post = postService.getByUrl(url);

        return postModel.post(post, token, model);
    }

    @GetMapping(value = "{url}/password")
    public String password(@PathVariable("url") String url,
                           Model model) {
        model.addAttribute("url", url);
        return "common/template/post_password";
    }

    @PostMapping(value = "{url}/password")
    @CacheLock(traceRequest = true, expired = 2)
    public String password(@PathVariable("url") String url,
                           @RequestParam(value = "password") String password) {
        Post post = postService.getBy(PostStatus.INTIMATE, url);

        if (password.equals(post.getPassword())) {
            String token = IdUtil.simpleUUID();
            cacheStore.putAny(token, token, 10, TimeUnit.SECONDS);

            String redirect = String.format("%s/archives/%s?token=%s", optionService.getBlogBaseUrl(), post.getUrl(), token);
            return "redirect:" + redirect;
        } else {
            String redirect = String.format("%s/archives/%s/password", optionService.getBlogBaseUrl(), post.getUrl());
            return "redirect:" + redirect;
        }
    }
}
