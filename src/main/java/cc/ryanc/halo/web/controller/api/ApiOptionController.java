package cc.ryanc.halo.web.controller.api;

import cc.ryanc.halo.model.dto.JsonResult;
import cc.ryanc.halo.service.OptionsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
     * <p>
     * result json:
     * <pre>
     * {
     *     "code": 200,
     *     "msg": "OK",
     *     "result": {
     *         "blog_start": "",
     *         "blog_locale": "",
     *         "blog_url": "",
     *         "api_token": "",
     *         "api_status": "",
     *         "blog_title": "",
     *         "new_comment_notice": "",
     *         "smtp_email_enable": "",
     *         "attach_loc": "",
     *         "theme": "",
     *         "comment_reply_notice": "",
     *         "is_install": "",
     *         "comment_pass_notice": ""
     *     }
     * }
     *     </pre>
     * </p>
     *
     * @return JsonResult
     */
    @GetMapping
    public Map<String, String> options() {
        return optionsService.findAllOptions();
    }

    /**
     * 获取单个设置项
     *
     * <p>
     * result json:
     * <pre>
     * {
     *     "code": 200,
     *     "msg": "OK",
     *     "result": ""
     * }
     *     </pre>
     * </p>
     *
     * @param optionName 设置选项名称
     *
     * @return JsonResult
     */
    @GetMapping(value = "/one")
    public JsonResult option(@RequestParam(value = "optionName") String optionName) {
        return new JsonResult(HttpStatus.OK.value(), HttpStatus.OK.getReasonPhrase(), optionsService.findOneOption(optionName));
    }
}
