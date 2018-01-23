package cc.ryanc.halo.web.controller.admin;

import cc.ryanc.halo.model.domain.Comment;
import cc.ryanc.halo.model.dto.HaloConst;
import cc.ryanc.halo.service.CommentService;
import cc.ryanc.halo.service.MailService;
import cc.ryanc.halo.service.PostService;
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

import javax.servlet.http.HttpSession;

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

    /**
     * 渲染评论管理页面
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
        model.addAttribute("options", HaloConst.OPTIONS);
        return "admin/comment";
    }

    /**
     * 将评论移到回收站
     * @param commentId commentId
     * @return string
     */
    @GetMapping(value = "/throw")
    public String moveToTrash(@RequestParam("commentId") Long commentId,
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
     * @param commentId commentId
     * @param status status
     * @return
     */
    @GetMapping("/revert")
    public String moveToPublish(@RequestParam("commentId") Long commentId,
                                @RequestParam("status") Integer status,
                                HttpSession session){
        try{
            Comment comment = commentService.updateCommentStatus(commentId,0);
            if(status==1){
                mailService.sendMail(comment.getCommentAuthorEmail(),"你在"+ HaloConst.OPTIONS.get("site_title")+"的评论已通过审核","你在"+comment.getPost().getPostTitle()+"的评论已经通过审核！");
            }
            this.getNewComments(session);
        }catch (Exception e){
            log.error("未知错误："+e.getMessage());
        }
        return "redirect:/admin/comments?status="+status;
    }
}
