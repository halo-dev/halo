package cc.ryanc.halo.web.controller.admin;

import cc.ryanc.halo.model.domain.User;
import cc.ryanc.halo.model.dto.JsonResult;
import cc.ryanc.halo.service.UserService;
import cn.hutool.crypto.SecureUtil;
import freemarker.template.Configuration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

/**
 * @author : RYAN0UP
 * @date : 2017/12/24
 */
@Slf4j
@Controller
@RequestMapping(value = "/admin/profile")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private Configuration configuration;

    /**
     * 获取用户信息并跳转
     *
     * @return 模板路径admin/admin_profile
     */
    @GetMapping
    public String profile() {
        return "admin/admin_profile";
    }

    /**
     * 处理修改用户资料的请求
     *
     * @param user    user
     * @param session session
     * @return JsonResult
     */
    @PostMapping(value = "save")
    @ResponseBody
    public JsonResult saveProfile(@ModelAttribute User user, HttpSession session) {
        try {
            if (null != user) {
                userService.saveByUser(user);
                configuration.setSharedVariable("user", userService.findUser());
                session.invalidate();
            } else {
                return new JsonResult(0,"修改失败！");
            }
        } catch (Exception e) {
            log.error("未知错误：{0}", e.getMessage());
            return new JsonResult(0,"修改失败！");
        }
        return new JsonResult(1,"修改成功！");
    }

    /**
     * 处理修改密码的请求
     *
     * @param beforePass 旧密码
     * @param newPass    新密码
     * @param userId     用户编号
     * @param session    session
     * @return JsonResult
     */
    @PostMapping(value = "changePass")
    @ResponseBody
    public JsonResult changePass(@ModelAttribute("beforePass") String beforePass,
                              @ModelAttribute("newPass") String newPass,
                              @ModelAttribute("userId") Long userId,
                              HttpSession session) {
        try {
            User user = userService.findByUserIdAndUserPass(userId, SecureUtil.md5(beforePass));
            if (null != user) {
                user.setUserPass(SecureUtil.md5(newPass));
                userService.saveByUser(user);
                session.invalidate();
            } else {
                return new JsonResult(0,"原密码错误！");
            }
        } catch (Exception e) {
            log.error("修改密码：未知错误，{0}", e.getMessage());
            return new JsonResult(0,"密码修改失败！");
        }
        return new JsonResult(1,"修改密码成功！");
    }
}
