package cc.ryanc.halo.web.controller.api;

import cc.ryanc.halo.exception.BadRequestException;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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

    private final Logger log = Logger.getLogger(getClass());

    @Autowired
    private CommentService commentService;

    @Autowired
    private PostService postService;

    /**
     * 新增评论
     *
     * @param comment comment
     * @param postId  postId
     * @param request request
     *
     * @return JsonResult
     */
    @PostMapping(value = "/save")
    @ResponseBody
    public JsonResult save(@Valid Comment comment,
                           @RequestParam(value = "postId") Long postId,
                           HttpServletRequest request) {
        try {
            Comment lastComment = null;
            final Post post = postService.fetchById(postId).orElse(new Post());
            comment.setCommentAuthorEmail(HtmlUtil.escape(comment.getCommentAuthorEmail()).toLowerCase());
            comment.setPost(post);
            comment.setCommentAuthorIp(ServletUtil.getClientIP(request));
            comment.setIsAdmin(0);
            comment.setCommentAuthor(HtmlUtil.escape(comment.getCommentAuthor()));
            if (StrUtil.isNotBlank(comment.getCommentAuthorEmail())) {
                comment.setCommentAuthorAvatarMd5(SecureUtil.md5(comment.getCommentAuthorEmail()));
            }
            if (comment.getCommentParent() > 0) {
                lastComment = commentService.fetchById(comment.getCommentParent()).orElse(new Comment());
                final StrBuilder buildContent = new StrBuilder("<a href='#comment-id-");
                buildContent.append(lastComment.getCommentId());
                buildContent.append("'>@");
                buildContent.append(lastComment.getCommentAuthor());
                buildContent.append("</a> ");
                buildContent.append(OwoUtil.markToImg(HtmlUtil.escape(comment.getCommentContent()).replace("&lt;br/&gt;", "<br/>")));
                comment.setCommentContent(buildContent.toString());
            } else {
                //将评论内容的字符专为安全字符
                comment.setCommentContent(OwoUtil.markToImg(HtmlUtil.escape(comment.getCommentContent()).replace("&lt;br/&gt;", "<br/>")));
            }
            if (StrUtil.isNotEmpty(comment.getCommentAuthorUrl())) {
                comment.setCommentAuthorUrl(URLUtil.normalize(comment.getCommentAuthorUrl()));
            }
            commentService.create(comment);
            if (StrUtil.equals(OPTIONS.get(BlogPropertiesEnum.NEW_COMMENT_NEED_CHECK.getProp()), TrueFalseEnum.TRUE.getDesc()) || OPTIONS.get(BlogPropertiesEnum.NEW_COMMENT_NEED_CHECK.getProp()) == null) {
                return new JsonResult(HttpStatus.OK.value(), "你的评论已经提交，待博主审核之后可显示。");
            } else {
                return new JsonResult(HttpStatus.OK.value(), "你的评论已经提交，刷新后即可显示。");
            }
        } catch (Exception e) {
            throw new BadRequestException("评论失败！", e);
        }
    }
}
