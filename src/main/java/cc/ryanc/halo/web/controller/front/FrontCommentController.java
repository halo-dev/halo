package cc.ryanc.halo.web.controller.front;

import cc.ryanc.halo.model.domain.Comment;
import cc.ryanc.halo.model.domain.Post;
import cc.ryanc.halo.model.dto.JsonResult;
import cc.ryanc.halo.model.enums.BlogPropertiesEnum;
import cc.ryanc.halo.model.enums.PostTypeEnum;
import cc.ryanc.halo.model.enums.ResultCodeEnum;
import cc.ryanc.halo.model.enums.TrueFalseEnum;
import cc.ryanc.halo.service.CommentService;
import cc.ryanc.halo.service.MailService;
import cc.ryanc.halo.service.PostService;
import cc.ryanc.halo.service.UserService;
import cc.ryanc.halo.utils.OwoUtil;
import cn.hutool.core.lang.Validator;
import cn.hutool.core.text.StrBuilder;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.extra.servlet.ServletUtil;
import cn.hutool.http.HtmlUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

import static cc.ryanc.halo.model.dto.HaloConst.OPTIONS;

/**
 * <pre>
 *     前台评论控制器
 * </pre>
 *
 * @author : RYAN0UP
 * @date : 2018/4/26
 */
@Slf4j
@Controller
public class FrontCommentController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private PostService postService;

    @Autowired
    private UserService userService;

    @Autowired
    private MailService mailService;

    /**
     * 提交新评论
     *
     * @param comment comment实体
     * @param post    post实体
     * @param request request
     * @return JsonResult
     */
    @PostMapping(value = "/newComment")
    @ResponseBody
    public JsonResult newComment(@Valid @ModelAttribute("comment") Comment comment,
                                 BindingResult result,
                                 @ModelAttribute("post") Post post,
                                 HttpServletRequest request) {
        if (result.hasErrors()) {
            for (ObjectError error : result.getAllErrors()) {
                return new JsonResult(ResultCodeEnum.FAIL.getCode(), error.getDefaultMessage());
            }
        }
        try {
            Comment lastComment = null;
            post = postService.fetchById(post.getPostId()).orElse(new Post());
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
            if (comment.getCommentParent() > 0) {
                new EmailToParent(comment, lastComment, post).start();
                new EmailToAdmin(comment, post).start();
            } else {
                new EmailToAdmin(comment, post).start();
            }
            if (StrUtil.equals(OPTIONS.get(BlogPropertiesEnum.NEW_COMMENT_NEED_CHECK.getProp()), TrueFalseEnum.TRUE.getDesc()) || OPTIONS.get(BlogPropertiesEnum.NEW_COMMENT_NEED_CHECK.getProp()) == null) {
                return new JsonResult(ResultCodeEnum.SUCCESS.getCode(), "你的评论已经提交，待博主审核之后可显示。");
            } else {
                return new JsonResult(ResultCodeEnum.SUCCESS.getCode(), "你的评论已经提交，刷新后即可显示。");
            }
        } catch (Exception e) {
            return new JsonResult(ResultCodeEnum.FAIL.getCode(), "评论失败！");
        }
    }

    /**
     * 发送邮件给博主
     */
    class EmailToAdmin extends Thread {
        private Comment comment;
        private Post post;

        private EmailToAdmin(Comment comment, Post post) {
            this.comment = comment;
            this.post = post;
        }

        @Override
        public void run() {
            if (StrUtil.equals(OPTIONS.get(BlogPropertiesEnum.SMTP_EMAIL_ENABLE.getProp()), TrueFalseEnum.TRUE.getDesc()) && StrUtil.equals(OPTIONS.get(BlogPropertiesEnum.NEW_COMMENT_NOTICE.getProp()), TrueFalseEnum.TRUE.getDesc())) {
                try {
                    //发送邮件到博主
                    final Map<String, Object> map = new HashMap<>(5);
                    final StrBuilder pageUrl = new StrBuilder(OPTIONS.get(BlogPropertiesEnum.BLOG_URL.getProp()));
                    if (StrUtil.equals(post.getPostType(), PostTypeEnum.POST_TYPE_POST.getDesc())) {
                        pageUrl.append("/archives/");
                    } else {
                        pageUrl.append("/p/");
                    }
                    pageUrl.append(post.getPostUrl());
                    pageUrl.append("#comment-id-");
                    pageUrl.append(comment.getCommentId());
                    map.put("pageUrl", pageUrl.toString());
                    map.put("author", userService.findUser().getUserDisplayName());
                    map.put("pageName", post.getPostTitle());
                    map.put("visitor", comment.getCommentAuthor());
                    map.put("commentContent", comment.getCommentContent());
                    mailService.sendTemplateMail(userService.findUser().getUserEmail(), "有新的评论", map, "common/mail_template/mail_admin.ftl");
                } catch (Exception e) {
                    log.error("Mail server not configured: {}", e.getMessage());
                }
            }
        }
    }

    /**
     * 发送邮件给被评论方
     */
    class EmailToParent extends Thread {
        private Comment comment;
        private Comment lastComment;
        private Post post;

        private EmailToParent(Comment comment, Comment lastComment, Post post) {
            this.comment = comment;
            this.lastComment = lastComment;
            this.post = post;
        }

        @Override
        public void run() {
            //发送通知给对方
            if (StrUtil.equals(OPTIONS.get(BlogPropertiesEnum.SMTP_EMAIL_ENABLE.getProp()), TrueFalseEnum.TRUE.getDesc()) && StrUtil.equals(OPTIONS.get(BlogPropertiesEnum.NEW_COMMENT_NOTICE.getProp()), TrueFalseEnum.TRUE.getDesc())) {
                if (Validator.isEmail(lastComment.getCommentAuthorEmail())) {
                    final Map<String, Object> map = new HashMap<>(8);
                    final StrBuilder pageUrl = new StrBuilder(OPTIONS.get(BlogPropertiesEnum.BLOG_URL.getProp()));
                    if (StrUtil.equals(post.getPostType(), PostTypeEnum.POST_TYPE_POST.getDesc())) {
                        pageUrl.append("/archives/");

                    } else {
                        pageUrl.append("/p/");
                    }
                    pageUrl.append(post.getPostUrl());
                    pageUrl.append("#comment-id-");
                    pageUrl.append(comment.getCommentId());
                    map.put("pageUrl", pageUrl.toString());
                    map.put("blogTitle", OPTIONS.get(BlogPropertiesEnum.BLOG_TITLE.getProp()));
                    map.put("commentAuthor", lastComment.getCommentAuthor());
                    map.put("pageName", lastComment.getPost().getPostTitle());
                    map.put("commentContent", lastComment.getCommentContent());
                    map.put("replyAuthor", comment.getCommentAuthor());
                    map.put("replyContent", comment.getCommentContent());
                    map.put("blogUrl", OPTIONS.get(BlogPropertiesEnum.BLOG_URL.getProp()));
                    mailService.sendTemplateMail(
                            lastComment.getCommentAuthorEmail(), "您在" + OPTIONS.get(BlogPropertiesEnum.BLOG_TITLE.getProp()) + "的评论有了新回复", map, "common/mail_template/mail_reply.ftl");
                }
            }
        }
    }
}

