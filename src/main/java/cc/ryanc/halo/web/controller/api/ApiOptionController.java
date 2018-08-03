package cc.ryanc.halo.web.controller.api;

import cc.ryanc.halo.model.dto.JsonResult;
import cc.ryanc.halo.model.enums.BlogPropertiesEnum;
import cc.ryanc.halo.model.enums.ResponseStatusEnum;
import cc.ryanc.halo.service.OptionsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * <pre>
 *     系统设置API
 * </pre>
 *
 * @author : RYAN0UP
 * @date : 2018/7/19
 */
@RestController
@RequestMapping(value = "/api/options")
public class ApiOptionController {

    @Autowired
    private OptionsService optionsService;

    /**
     * 获取所有设置选项
     *
     * @return JsonResult
     */
    @GetMapping
    public JsonResult options() {
        Map<String, String> options = optionsService.findAllOptions();
        //去掉隐私元素
        options.remove(BlogPropertiesEnum.MAIL_SMTP_HOST.getProp());
        options.remove(BlogPropertiesEnum.MAIL_FROM_NAME.getProp());
        options.remove(BlogPropertiesEnum.MAIL_SMTP_PASSWORD.getProp());
        options.remove(BlogPropertiesEnum.MAIL_SMTP_USERNAME.getProp());
        options.remove(BlogPropertiesEnum.MAIL_SMTP_USERNAME.getProp());
        return new JsonResult(ResponseStatusEnum.SUCCESS.getCode(), ResponseStatusEnum.SUCCESS.getMsg(), options);
    }

    /**
     * 获取单个设置项
     *
     * @param optionName 设置选项名称
     * @return JsonResult
     */
    @GetMapping(value = "/{optionName}")
    public JsonResult option(@PathVariable(value = "optionName") String optionName) {
        String optionValue = optionsService.findOneOption(optionName);
        return new JsonResult(ResponseStatusEnum.SUCCESS.getCode(), ResponseStatusEnum.SUCCESS.getMsg(), optionValue);
    }
}
