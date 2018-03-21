package cc.ryanc.halo.web.controller.admin;

import cc.ryanc.halo.model.domain.User;
import cc.ryanc.halo.model.dto.HaloConst;
import cc.ryanc.halo.model.dto.RespStatus;
import cc.ryanc.halo.service.UserService;
import cc.ryanc.halo.util.HaloUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

/**
 * @author : RYAN0UP
 * @date : 2017/12/24
 * @version : 1.0
 * description: 用户控制
 */
@Slf4j
@Controller
@RequestMapping(value = "/admin/profile")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 获取用户信息并跳转
     *
     * @return string
     */
    @GetMapping
    public String profile(Model model){
        model.addAttribute("user",userService.findAllUser().get(0));
        //设置选项
        model.addAttribute("options",HaloConst.OPTIONS);
        return "admin/admin_profile";
    }

    /**
     * 处理修改用户资料的请求
     *
     * @param user user
     * @return String
     */
    @PostMapping(value = "save")
    @ResponseBody
    public String saveProfile(@ModelAttribute User user,HttpSession session){
        try{
            if(null!=user){
                userService.saveByUser(user);
                session.invalidate();
            }else{
                log.error("用户信息不能为空值");
                return RespStatus.ERROR;
            }
        }catch (Exception e){
            log.error("未知错误："+e.getMessage());
            return RespStatus.ERROR;
        }
        return RespStatus.SUCCESS;
    }

    /**
     * 处理修改密码的请求
     *
     * @param beforePass 之前的密码
     * @param newPass 新密码
     * @return String
     */
    @PostMapping(value = "changePass")
    @ResponseBody
    public String changePass(@ModelAttribute("beforePass") String beforePass,
                             @ModelAttribute("newPass") String newPass,
                             @ModelAttribute("userId") Long userId,
                             HttpSession session){
        try {
            User user = userService.findByUserIdAndUserPass(userId,HaloUtil.getMD5(beforePass));
            if(null!=user){
                user.setUserPass(HaloUtil.getMD5(newPass));
                userService.saveByUser(user);
                log.info("修改密码：成功");
                session.invalidate();
            }else{
                log.error("修改密码：原密码错误！");
                return RespStatus.ERROR;
            }
        }catch (Exception e){
            log.error("修改密码：未知错误，"+e.getMessage());
            return RespStatus.ERROR;
        }
        return RespStatus.SUCCESS;
    }
}
