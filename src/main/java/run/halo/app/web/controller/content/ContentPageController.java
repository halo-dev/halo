package run.halo.app.web.controller.content;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import run.halo.app.exception.NotFoundException;
import run.halo.app.model.entity.Comment;
import run.halo.app.model.entity.Gallery;
import run.halo.app.model.entity.Post;
import run.halo.app.model.enums.PostStatus;
import run.halo.app.service.CommentService;
import run.halo.app.service.GalleryService;
import run.halo.app.service.PostService;
import run.halo.app.service.ThemeService;

import java.util.List;

/**
 * @author : RYAN0UP
 * @date : 2019-03-21
 */
@Controller
public class ContentPageController {

    private final GalleryService galleryService;

    private final PostService postService;

    private final CommentService commentService;

    private final ThemeService themeService;

    public ContentPageController(GalleryService galleryService,
                                 PostService postService,
                                 CommentService commentService,
                                 ThemeService themeService) {
        this.galleryService = galleryService;
        this.postService = postService;
        this.commentService = commentService;
        this.themeService = themeService;
    }

    /**
     * Render gallery page
     *
     * @return template path: themes/{theme}/gallery
     */
    @GetMapping(value = "/gallery")
    public String gallery(Model model) {
        final List<Gallery> galleries = galleryService.listAll();
        model.addAttribute("galleries", galleries);
        return themeService.render("gallery");
    }

    /**
     * Render links page
     *
     * @return template path: themes/{theme}/links
     */
    @GetMapping(value = "/links")
    public String links() {
        return themeService.render("links");
    }

    /**
     * Render custom page
     *
     * @param url   page url
     * @param model model
     * @return template path: themes/{theme}/post
     */
    @GetMapping(value = "/p/{url}")
    public String getPage(@PathVariable(value = "url") String url,
                          @RequestParam(value = "cp", defaultValue = "1") Integer cp,
                          Model model) {
        final Post post = postService.getByUrl(url);

        if (!post.getStatus().equals(PostStatus.PUBLISHED)) {
            throw new NotFoundException("The post isn't published").setErrorData(url);
        }

        List<Comment> comments;

        // TODO Complete this api

//        if (StrUtil.equals(OPTIONS.get(BlogProperties.NEW_COMMENT_NEED_CHECK.getValue()), "true") || OPTIONS.get(BlogProperties.NEW_COMMENT_NEED_CHECK.getValue()) == null) {
//            comments = commentService.findCommentsByPostAndCommentStatus(post, CommentStatus.PUBLISHED.getValue());
//        } else {
//            comments = commentService.findCommentsByPostAndCommentStatusNot(post, CommentStatusEnum.RECYCLE.getCode());
//        }
//        //默认显示10条
//        int size = 10;
//        if (StrUtil.isNotBlank(OPTIONS.get(BlogPropertiesEnum.INDEX_COMMENTS.getProp()))) {
//            size = Integer.parseInt(OPTIONS.get(BlogPropertiesEnum.INDEX_COMMENTS.getProp()));
//        }
//        //评论分页
//        final ListPage<Comment> commentsPage = new ListPage<>(CommentUtil.getComments(comments), cp, size);
//        final int[] rainbow = PageUtil.rainbow(cp, commentsPage.getTotalPage(), 3);
//        model.addAttribute("is_page", true);
//        model.addAttribute("post", post);
//        model.addAttribute("comments", commentsPage);
//        model.addAttribute("commentsCount", comments.size());
//        model.addAttribute("rainbow", rainbow);
//        postService.cacheViews(post.getPostId());
//
//        //如果设置了自定义模板，则渲染自定义模板
//        if (StrUtil.isNotEmpty(post.getCustomTpl())) {
//            return this.render(post.getCustomTpl());
//        }
        return themeService.render("page");
    }
}
