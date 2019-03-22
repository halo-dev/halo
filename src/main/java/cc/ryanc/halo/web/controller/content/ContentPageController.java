package cc.ryanc.halo.web.controller.content;

import cc.ryanc.halo.model.entity.Comment;
import cc.ryanc.halo.model.entity.Gallery;
import cc.ryanc.halo.model.entity.Post;
import cc.ryanc.halo.model.enums.PostStatus;
import cc.ryanc.halo.model.enums.PostType;
import cc.ryanc.halo.service.CommentService;
import cc.ryanc.halo.service.GalleryService;
import cc.ryanc.halo.service.PostService;
import cc.ryanc.halo.web.controller.content.base.BaseContentController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @author : RYAN0UP
 * @date : 2019-03-21
 */
@Controller
public class ContentPageController extends BaseContentController {

    private final GalleryService galleryService;

    private final PostService postService;

    private final CommentService commentService;

    public ContentPageController(GalleryService galleryService, PostService postService, CommentService commentService) {
        this.galleryService = galleryService;
        this.postService = postService;
        this.commentService = commentService;
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
        return this.render("gallery");
    }

    /**
     * Render links page
     *
     * @return template path: themes/{theme}/links
     */
    @GetMapping(value = "/links")
    public String links() {
        return this.render("links");
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
        if (null == post || !post.getStatus().equals(PostStatus.PUBLISHED)) {
            return this.renderNotFound();
        }
        List<Comment> comments;
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
        return this.render("page");
    }
}
