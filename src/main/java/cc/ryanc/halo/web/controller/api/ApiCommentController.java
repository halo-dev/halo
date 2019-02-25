package cc.ryanc.halo.web.controller.api;

import cc.ryanc.halo.logging.Logger;
import cc.ryanc.halo.model.domain.Comment;
import cc.ryanc.halo.model.domain.Post;
import cc.ryanc.halo.model.dto.JsonResult;
import cc.ryanc.halo.model.enums.BlogPropertiesEnum;
import cc.ryanc.halo.model.enums.TrueFalseEnum;
import cc.ryanc.halo.service.CommentService;
import cc.ryanc.halo.service.PostService;
import cc.ryanc.halo.utils.OwoUtil;
import cn.hutool.core.text.StrBuilder;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.extra.servlet.ServletUtil;
import cn.hutool.http.HtmlUtil;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import static cc.ryanc.halo.model.dto.HaloConst.OPTIONS;

/**
 * <pre>
 *     评论API
 * </pre>
 *
 * @author : RYAN0UP
 * @date : 2018/6/6
 */
@RestController
@RequestMapping(value = "/api/comments")
public class ApiCommentController {

    /**
     * Comment format [formatter 1: commentId, formatter 2: commentAuthor, formatter 3: commentContent]
     */
    private final static String COMMENT_FORMAT = "<a href='#comment-id-%s'>@%s</a> %s";

    private final CommentService commentService;

    private final PostService postService;

    public ApiCommentController(CommentService commentService, PostService postService) {
        this.commentService = commentService;
        this.postService = postService;
    }

    /**
     * 新增评论
     *
     * @param comment comment
     * @param postId  postId
     * @param request request
     * @return JsonResult
     */
    @PostMapping("save")
    public JsonResult save(@Valid Comment comment,
                           @RequestParam(value = "postId") Long postId,
                           HttpServletRequest request) {
        comment.setCommentAuthorEmail(HtmlUtil.escape(comment.getCommentAuthorEmail()).toLowerCase());
        comment.setPost(postService.fetchById(postId).orElse(new Post()));
        comment.setCommentAuthorIp(ServletUtil.getClientIP(request));
        comment.setIsAdmin(0);
        comment.setCommentAuthor(HtmlUtil.escape(comment.getCommentAuthor()));

        if (StrUtil.isNotBlank(comment.getCommentAuthorEmail())) {
            comment.setCommentAuthorAvatarMd5(SecureUtil.md5(comment.getCommentAuthorEmail()));
        }
        if (comment.getCommentParent() > 0) {
            // Get last comment
            Comment lastComment = commentService.fetchById(comment.getCommentParent()).orElse(new Comment());
            // Format and set comment content
            comment.setCommentContent(String.format(COMMENT_FORMAT, lastComment.getCommentId(), lastComment.getCommentAuthor(), convertToSecureString(comment.getCommentContent())));
        } else {
            //将评论内容的字符专为安全字符
            comment.setCommentContent(convertToSecureString(comment.getCommentContent()));
        }
        if (StrUtil.isNotEmpty(comment.getCommentAuthorUrl())) {
            comment.setCommentAuthorUrl(URLUtil.normalize(comment.getCommentAuthorUrl()));
        }

        // Create the comment
        commentService.create(comment);

        if (StrUtil.equals(OPTIONS.get(BlogPropertiesEnum.NEW_COMMENT_NEED_CHECK.getProp()), TrueFalseEnum.TRUE.getDesc()) || OPTIONS.get(BlogPropertiesEnum.NEW_COMMENT_NEED_CHECK.getProp()) == null) {
            return JsonResult.ok("你的评论已经提交，待博主审核之后可显示。");
        } else {
            return JsonResult.ok("你的评论已经提交，刷新后即可显示。");
        }
    }

    /**
     * Converts content to secure content.
     *
     * @param originalContent original content must not be null
     * @return secure content
     */
    @NonNull
    private String convertToSecureString(@NonNull String originalContent) {
        Assert.hasText(originalContent, "Original content must not be blank");

        return OwoUtil.markToImg(HtmlUtil.escape(originalContent).replace("&lt;br/&gt;", "<br/>"));
    }
}
