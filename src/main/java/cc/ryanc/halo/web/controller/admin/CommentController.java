package cc.ryanc.halo.web.controller.admin;

import cc.ryanc.halo.model.domain.Comment;
import cc.ryanc.halo.model.domain.Post;
import cc.ryanc.halo.model.domain.User;
import cc.ryanc.halo.model.dto.JsonResult;
import cc.ryanc.halo.model.enums.*;
import cc.ryanc.halo.service.CommentService;
import cc.ryanc.halo.service.MailService;
import cc.ryanc.halo.service.PostService;
import cc.ryanc.halo.utils.OwoUtil;
import cc.ryanc.halo.web.controller.core.BaseController;
import cn.hutool.core.lang.Validator;
import cn.hutool.core.text.StrBuilder;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.extra.servlet.ServletUtil;
import cn.hutool.http.HtmlUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

import static cc.ryanc.halo.model.dto.HaloConst.OPTIONS;
import static cc.ryanc.halo.model.dto.HaloConst.USER_SESSION_KEY;

/**
 * <pre>
 *     后台评论管理控制器
 * </pre>
 *
 * @author : RYAN0UP
 * @date : 2017/12/10
 */
@Slf4j
@Controller
@RequestMapping(value = "/admin/comments")
public class CommentController extends BaseController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private MailService mailService;

    @Autowired
    private PostService postService;

    /**
     * 渲染评论管理页面
     *
     * @param model  model
     * @param status status 评论状态
     * @return 模板路径admin/admin_comment
     */
    @GetMapping
    public String comments(Model model,
                           @PageableDefault(sort = "commentDate", direction = Sort.Direction.DESC) Pageable pageable,
                           @RequestParam(value = "status", defaultValue = "0") Integer status) {
        final Page<Comment> comments = commentService.findAll(status, pageable);
        model.addAttribute("comments", comments);
        model.addAttribute("publicCount", commentService.getCountByStatus(CommentStatusEnum.PUBLISHED.getCode()));
        model.addAttribute("checkCount", commentService.getCountByStatus(CommentStatusEnum.CHECKING.getCode()));
        model.addAttribute("trashCount", commentService.getCountByStatus(CommentStatusEnum.RECYCLE.getCode()));
        model.addAttribute("status", status);
        return "admin/admin_comment";
    }

    /**
     * 将评论移到回收站
     *
     * @param commentId 评论编号
     * @param status    评论状态
     * @return 重定向到/admin/comments
     */
    @GetMapping(value = "/throw")
    public String moveToTrash(@RequestParam("commentId") Long commentId,
                              @RequestParam("status") String status,
                              @PageableDefault Pageable pageable) {
        try {
            commentService.updateCommentStatus(commentId, CommentStatusEnum.RECYCLE.getCode());
        } catch (Exception e) {
            log.error("Delete comment failed: {}", e.getMessage());
        }
        return "redirect:/admin/comments?status=" + status + "&page=" + pageable.getPageNumber();
    }

    /**
     * 将评论改变为发布状态
     *
     * @param commentId 评论编号
     * @param status    评论状态
     * @param session   session
     * @return 重定向到/admin/comments
     */
    @GetMapping(value = "/revert")
    public String moveToPublish(@RequestParam("commentId") Long commentId,
                                @RequestParam("status") Integer status,
                                HttpSession session) {
        final Comment comment = commentService.updateCommentStatus(commentId, CommentStatusEnum.PUBLISHED.getCode());
        final Post post = comment.getPost();
        final User user = (User) session.getAttribute(USER_SESSION_KEY);

        //判断是否启用邮件服务
        new NoticeToAuthor(comment, post, user, status).start();
        return "redirect:/admin/comments?status=" + status;
    }

    /**
     * 删除评论
     *
     * @param commentId commentId 评论编号
     * @param status    status 评论状态
     * @return string 重定向到/admin/comments
     */
    @GetMapping(value = "/remove")
    public String moveToAway(@RequestParam("commentId") Long commentId,
                             @RequestParam("status") Integer status,
                             @PageableDefault Pageable pageable) {
        try {
            commentService.remove(commentId);
        } catch (Exception e) {
            log.error("Delete comment failed: {}", e.getMessage());
        }
        return "redirect:/admin/comments?status=" + status + "&page=" + pageable.getPageNumber();
    }


    /**
     * 管理员回复评论
     *
     * @param commentId      被回复的评论
     * @param commentContent 回复的内容
     * @return JsonResult
     */
    @PostMapping(value = "/reply")
    @ResponseBody
    public JsonResult replyComment(@RequestParam("commentId") Long commentId,
                                   @RequestParam("postId") Long postId,
                                   @RequestParam("commentContent") String commentContent,
                                   @RequestParam("userAgent") String userAgent,
                                   HttpServletRequest request,
                                   HttpSession session) {
        try {
            final Post post = postService.fetchById(postId).orElse(new Post());

            //博主信息
            final User user = (User) session.getAttribute(USER_SESSION_KEY);

            //被回复的评论
            final Comment lastComment = commentService.fetchById(commentId).orElse(new Comment());

            //修改被回复的评论的状态
            lastComment.setCommentStatus(CommentStatusEnum.PUBLISHED.getCode());
            commentService.create(lastComment);

            //保存评论
            final Comment comment = new Comment();
            comment.setPost(post);
            comment.setCommentAuthor(user.getUserDisplayName());
            comment.setCommentAuthorEmail(user.getUserEmail());
            comment.setCommentAuthorUrl(OPTIONS.get(BlogPropertiesEnum.BLOG_URL.getProp()));
            comment.setCommentAuthorIp(ServletUtil.getClientIP(request));
            comment.setCommentAuthorAvatarMd5(SecureUtil.md5(user.getUserEmail()));

            final StrBuilder buildContent = new StrBuilder("<a href='#comment-id-");
            buildContent.append(lastComment.getCommentId());
            buildContent.append("'>@");
            buildContent.append(lastComment.getCommentAuthor());
            buildContent.append("</a> ");
            buildContent.append(OwoUtil.markToImg(HtmlUtil.escape(commentContent).replace("&lt;br/&gt;", "<br/>")));

            comment.setCommentContent(buildContent.toString());
            comment.setCommentAgent(userAgent);
            comment.setCommentParent(commentId);
            comment.setCommentStatus(CommentStatusEnum.PUBLISHED.getCode());
            comment.setIsAdmin(1);
            commentService.create(comment);

            //邮件通知
            new EmailToAuthor(comment, lastComment, post, user, commentContent).start();
            return new JsonResult(ResultCodeEnum.SUCCESS.getCode());
        } catch (Exception e) {
            log.error("Reply to comment failed: {}", e.getMessage());
            return new JsonResult(ResultCodeEnum.FAIL.getCode());
        }
    }

    /**
     * 异步发送邮件回复给评论者
     */
    class EmailToAuthor extends Thread {

        private Comment comment;
        private Comment lastComment;
        private Post post;
        private User user;
        private String commentContent;

        private EmailToAuthor(Comment comment, Comment lastComment, Post post, User user, String commentContent) {
            this.comment = comment;
            this.lastComment = lastComment;
            this.post = post;
            this.user = user;
            this.commentContent = commentContent;
        }

        @Override
        public void run() {
            if (StrUtil.equals(OPTIONS.get(BlogPropertiesEnum.SMTP_EMAIL_ENABLE.getProp()), TrueFalseEnum.TRUE.getDesc()) && StrUtil.equals(OPTIONS.get(BlogPropertiesEnum.COMMENT_REPLY_NOTICE.getProp()), TrueFalseEnum.TRUE.getDesc())) {
                if (Validator.isEmail(lastComment.getCommentAuthorEmail())) {
                    final Map<String, Object> map = new HashMap<>(8);
                    map.put("blogTitle", OPTIONS.get(BlogPropertiesEnum.BLOG_TITLE.getProp()));
                    map.put("commentAuthor", lastComment.getCommentAuthor());
                    map.put("pageName", lastComment.getPost().getPostTitle());

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
                    map.put("commentContent", lastComment.getCommentContent());
                    map.put("replyAuthor", user.getUserDisplayName());
                    map.put("replyContent", commentContent);
                    map.put("blogUrl", OPTIONS.get(BlogPropertiesEnum.BLOG_URL.getProp()));
                    mailService.sendTemplateMail(
                            lastComment.getCommentAuthorEmail(), "您在" + OPTIONS.get(BlogPropertiesEnum.BLOG_URL.getProp()) + "的评论有了新回复", map, "common/mail_template/mail_reply.ftl");
                }
            }
        }
    }

    /**
     * 异步通知评论者审核通过
     */
    class NoticeToAuthor extends Thread {

        private Comment comment;
        private Post post;
        private User user;
        private Integer status;

        private NoticeToAuthor(Comment comment, Post post, User user, Integer status) {
            this.comment = comment;
            this.post = post;
            this.user = user;
            this.status = status;
        }

        @Override
        public void run() {
            if (StrUtil.equals(OPTIONS.get(BlogPropertiesEnum.SMTP_EMAIL_ENABLE.getProp()), TrueFalseEnum.TRUE.getDesc()) && StrUtil.equals(OPTIONS.get(BlogPropertiesEnum.COMMENT_REPLY_NOTICE.getProp()), TrueFalseEnum.TRUE.getDesc())) {
                try {
                    if (status == 1 && Validator.isEmail(comment.getCommentAuthorEmail())) {
                        final Map<String, Object> map = new HashMap<>(6);

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
                        map.put("pageName", post.getPostTitle());
                        map.put("commentContent", comment.getCommentContent());
                        map.put("blogUrl", OPTIONS.get(BlogPropertiesEnum.BLOG_URL.getProp()));
                        map.put("blogTitle", OPTIONS.get(BlogPropertiesEnum.BLOG_TITLE.getProp()));
                        map.put("author", user.getUserDisplayName());
                        mailService.sendTemplateMail(
                                comment.getCommentAuthorEmail(),
                                "您在" + OPTIONS.get(BlogPropertiesEnum.BLOG_URL.getProp()) + "的评论已审核通过！", map, "common/mail_template/mail_passed.ftl");
                    }
                } catch (Exception e) {
                    log.error("Mail server not configured: {}", e.getMessage());
                }
            }
        }
    }
}
