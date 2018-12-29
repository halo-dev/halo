package cc.ryanc.halo.web.controller.admin;

import cc.ryanc.halo.model.dto.HaloConst;
import cc.ryanc.halo.model.dto.JsonResult;
import cc.ryanc.halo.model.enums.ResultCodeEnum;
import cc.ryanc.halo.service.OptionsService;
import cc.ryanc.halo.utils.LocaleMessageUtil;
import freemarker.template.Configuration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import javax.servlet.http.HttpSession;
import java.util.Map;

/**
 * <pre>
 *     后台设置选项控制器
 * </pre>
 *
 * @author : RYAN0UP
 * @date : 2017/12/13
 */
@Slf4j
@Controller
@RequestMapping("/admin/option")
public class OptionController {

    @Autowired
    private OptionsService optionsService;

    @Autowired
    private Configuration configuration;

    @Autowired
    private LocaleMessageUtil localeMessageUtil;

    /**
     * 请求跳转到option页面并完成渲染
     *
     * @return 模板路径admin/admin_option
     */
    @GetMapping
    public String options() {
        return "admin/admin_option";
    }

    /**
     * 保存设置选项
     *
     * @param options options
     *
     * @return JsonResult
     */
    @PostMapping(value = "/save")
    @ResponseBody
    public JsonResult saveOptions(@RequestParam Map<String, String> options, HttpSession session) {
        try {
            optionsService.saveOptions(options);
            //刷新options
            configuration.setSharedVariable("options", optionsService.findAllOptions());
            HaloConst.OPTIONS.clear();
            HaloConst.OPTIONS = optionsService.findAllOptions();
            session.removeAttribute(SessionLocaleResolver.LOCALE_SESSION_ATTRIBUTE_NAME);
            log.info("List of saved options: " + options);
            return new JsonResult(ResultCodeEnum.SUCCESS.getCode(), localeMessageUtil.getMessage("code.admin.common.save-success"));
        } catch (Exception e) {
            log.error("Save settings option failed: {}", e.getMessage());
            return new JsonResult(ResultCodeEnum.FAIL.getCode(), localeMessageUtil.getMessage("code.admin.common.save-failed"));
        }
    }
}
