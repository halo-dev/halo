package cc.ryanc.halo.web.controller.admin;

import cc.ryanc.halo.model.dto.HaloConst;
import cc.ryanc.halo.web.controller.BaseController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author : RYAN0UP
 * @date : 2017/12/10
 * @version : 1.0
 * description : 评论系统管理
 */
@Controller
@RequestMapping(value = "/admin/comment")
public class CommentController extends BaseController{
    @GetMapping
    public String comments(Model model){
        model.addAttribute("options", HaloConst.OPTIONS);
        return "admin/comment";
    }
}
