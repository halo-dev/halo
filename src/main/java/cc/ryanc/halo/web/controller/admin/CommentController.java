package cc.ryanc.halo.web.controller.admin;

import cc.ryanc.halo.model.domain.Comment;
import cc.ryanc.halo.model.domain.Post;
import cc.ryanc.halo.model.dto.HaloConst;
import cc.ryanc.halo.service.CommentService;
import cc.ryanc.halo.service.MailService;
import cc.ryanc.halo.service.UserService;
import cc.ryanc.halo.util.HaloUtil;
import cc.ryanc.halo.web.controller.BaseController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.websocket.server.PathParam;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author : RYAN0UP
 * @date : 2017/12/10
 * @version : 1.0
 * description : 评论系统管理
 */
@Slf4j
@Controller
@RequestMapping(value = "/admin/comments")
public class CommentController extends BaseController{

    @Autowired
    private CommentService commentService;

    @Autowired
    private MailService mailService;

    @Autowired
    private UserService userService;

    /**
     * 渲染评论管理页面
     *
     * @param model model
     * @param status status
     * @param page page
     * @param size size
     * @return string
     */
    @GetMapping
    public String comments(Model model,
                           @RequestParam(value = "status",defaultValue = "0") Integer status,
                           @RequestParam(value = "page",defaultValue = "0") Integer page,
                           @RequestParam(value = "size",defaultValue = "10") Integer size){
        Sort sort = new Sort(Sort.Direction.DESC,"commentDate");
        Pageable pageable = new PageRequest(page,size,sort);
        Page<Comment> comments = commentService.findAllComments(status,pageable);
        model.addAttribute("comments",comments);
        model.addAttribute("publicCount",commentService.findAllComments(0,pageable).getTotalElements());
        model.addAttribute("checkCount",commentService.findAllComments(1,pageable).getTotalElements());
        model.addAttribute("trashCount",commentService.findAllComments(2,pageable).getTotalElements());
        model.addAttribute("status",status);

        //设置选项
        model.addAttribute("options",HaloConst.OPTIONS);
        return "admin/admin_comment";
    }

    /**
     * 将评论移到回收站
     *
     * @param commentId commentId
     * @return string
     */
    @GetMapping(value = "/throw")
    public String moveToTrash(@PathParam("commentId") Long commentId,
                              HttpSession session){
        try {
            commentService.updateCommentStatus(commentId,2);
            this.getNewComments(session);
        }catch (Exception e){
            log.error("未知错误："+e.getMessage());
        }
        return "redirect:/admin/comments";
    }

    /**
     * 将评论改变为发布状态
     *
     * @param commentId commentId
     * @param status status
     * @return string
     */
    @GetMapping("/revert")
    public String moveToPublish(@PathParam("commentId") Long commentId,
                                @PathParam("status") Integer status,
                                HttpSession session){
        Comment comment = commentService.updateCommentStatus(commentId,0);

        Pattern patternEmail = Pattern.compile("\\w[-\\w.+]*@([A-Za-z0-9][-A-Za-z0-9]+\\.)+[A-Za-z]{2,14}");
        Matcher matcher = patternEmail.matcher(comment.getCommentAuthorEmail());

        //判断是否启用邮件服务
        if("true".equals(HaloConst.OPTIONS.get("smtp_email_enable"))) {
            try {
                if (status == 1 && matcher.find()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("pageUrl", comment.getPost().getPostUrl());
                    map.put("pageName", comment.getPost().getPostTitle());
                    map.put("commentContent", comment.getCommentContent());
                    map.put("siteUrl", HaloConst.OPTIONS.get("site_url"));
                    map.put("siteTitle", HaloConst.OPTIONS.get("site_title"));
                    map.put("author", userService.findAllUser().get(0).getUserDisplayName());
                    mailService.sendTemplateMail(
                            comment.getCommentAuthorEmail(),
                            "您在" + HaloConst.OPTIONS.get("site_title") + "的评论已审核通过！", map, "common/mail/mail_passed.ftl");
                }
            } catch (Exception e) {
                log.error("邮件服务器未配置：" + e.getMessage());
            }
        }
        this.getNewComments(session);
        return "redirect:/admin/comments?status="+status;
    }

    /**
     * 删除评论
     *
     * @param commentId commentId
     * @param status status
     * @param session session
     * @return string
     */
    @GetMapping("/remove")
    public String moveToAway(@PathParam("commentId") Long commentId,
                             @PathParam("status") Integer status,
                             HttpSession session){
        try{
            commentService.removeByCommentId(commentId);
            this.getNewComments(session);
        }catch (Exception e){
            log.error("删除评论失败："+e.getMessage());
        }
        return "redirect:/admin/comments?status="+status;
    }


    /**
     * 管理员回复评论
     *
     * @param commentId 被回复的评论
     * @param commentContent 回复的内容
     * @return string
     */
    @PostMapping("/reply")
    public String replyComment(@RequestParam("commentId") Long commentId,
                               @RequestParam("postId") Long postId,
                               @RequestParam("commentContent") String commentContent,
                               @RequestParam("userAgent") String userAgent,
                               HttpServletRequest request){
        try {
            Post post = new Post();
            post.setPostId(postId);

            //保存评论
            Comment comment = new Comment();
            comment.setPost(post);
            comment.setCommentAuthor(userService.findAllUser().get(0).getUserDisplayName());
            comment.setCommentAuthorEmail(userService.findAllUser().get(0).getUserEmail());
            comment.setCommentAuthorUrl(HaloConst.OPTIONS.get("site_url"));
            comment.setCommentAuthorIp(HaloUtil.getIpAddr(request));
            comment.setCommentAuthorAvatarMd5(HaloUtil.getMD5(userService.findAllUser().get(0).getUserEmail()));
            comment.setCommentDate(new Date());
            String at = "<a href='#'>@"+commentService.findCommentById(commentId).get().getCommentAuthor()+"</a>";
            comment.setCommentContent(at+commentContent);
            comment.setCommentAgent(userAgent);
            comment.setCommentParent(commentId);
            comment.setCommentStatus(0);
            commentService.saveByComment(comment);
        }catch (Exception e){
            log.error("回复评论失败！"+e.getMessage());
        }
        return "redirect:/admin/comments";
    }
}
