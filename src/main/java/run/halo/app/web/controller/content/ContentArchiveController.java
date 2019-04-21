package run.halo.app.web.controller.content;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import run.halo.app.model.entity.Category;
import run.halo.app.model.entity.Post;
import run.halo.app.model.entity.Tag;
import run.halo.app.model.enums.PostStatus;
import run.halo.app.service.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;

/**
 * Blog archive page controller
 *
 * @author : RYAN0UP
 * @date : 2019-03-17
 */
@Controller
@RequestMapping(value = "/archives")
public class ContentArchiveController {

    private final PostService postService;

    private final CommentService commentService;

    private final ThemeService themeService;

    private final PostCategoryService postCategoryService;

    private final PostTagService postTagService;

    public ContentArchiveController(PostService postService,
                                    CommentService commentService,
                                    ThemeService themeService,
                                    PostCategoryService postCategoryService,
                                    PostTagService postTagService) {
        this.postService = postService;
        this.commentService = commentService;
        this.themeService = themeService;
        this.postCategoryService = postCategoryService;
        this.postTagService = postTagService;
    }

    /**
     * Render post page.
     *
     * @param url     post slug url.
     * @param cp      comment page number
     * @param request request
     * @param model   model
     * @return template path: theme/{theme}/post.ftl
     */
    @GetMapping(value = "{url}")
    public String post(@PathVariable String url,
                       @RequestParam(value = "cp", defaultValue = "1") Integer cp,
                       HttpServletRequest request,
                       Model model) {
        final Post post = postService.getByUrl(url);
        if (null == post || !post.getStatus().equals(PostStatus.PUBLISHED)) {
            return "redirect:/404";
        }

        final Date publishTime = post.getCreateTime();
        final Post nextPost = postService.getNextPostOfNullable(publishTime);
        final Post prePost = postService.getPrePostOfNullable(publishTime);

        if (null != prePost) {
            model.addAttribute("prePost", prePost);
        }
        if (null != nextPost) {
            model.addAttribute("nextPost", nextPost);
        }


        List<Category> categories = postCategoryService.listCategoryBy(post.getId());
        List<Tag> tags = postTagService.listTagsBy(post.getId());

        model.addAttribute("is_post", true);
        model.addAttribute("post",post);
        model.addAttribute("categories", categories);
        model.addAttribute("tags", tags);
        return themeService.render("post");
    }
}
