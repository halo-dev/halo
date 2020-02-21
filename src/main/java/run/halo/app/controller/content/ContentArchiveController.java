package run.halo.app.controller.content;

import cn.hutool.core.util.IdUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import run.halo.app.cache.StringCacheStore;
import run.halo.app.cache.lock.CacheLock;
import run.halo.app.model.entity.Post;
import run.halo.app.model.enums.PostStatus;
import run.halo.app.service.OptionService;
import run.halo.app.service.PostService;

import java.util.concurrent.TimeUnit;

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

    private final OptionService optionService;

    private final StringCacheStore cacheStore;


    public ContentArchiveController(PostService postService,
                                    OptionService optionService,
                                    StringCacheStore cacheStore) {
        this.postService = postService;
        this.optionService = optionService;
        this.cacheStore = cacheStore;
    }

    @GetMapping(value = "{url:.*}/password")
    public String password(@PathVariable("url") String url,
                           Model model) {
        model.addAttribute("url", url);
        return "common/template/post_password";
    }

    @PostMapping(value = "{url:.*}/password")
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
