package run.halo.app.controller.admin.api;

import io.swagger.annotations.ApiOperation;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import run.halo.app.annotation.DisableOnCondition;
import run.halo.app.mail.MailService;
import run.halo.app.model.params.MailParam;
import run.halo.app.model.support.BaseResponse;

/**
 * Mail controller.
 *
 * @author johnniang
 * @date 2019-05-07
 */
@RestController
@RequestMapping("/api/admin/mails")
public class MailController {

    private final MailService mailService;

    public MailController(MailService mailService) {
        this.mailService = mailService;
    }

    @PostMapping("test")
    @ApiOperation("Tests the SMTP service")
    @DisableOnCondition
    public BaseResponse<String> testMail(@Valid @RequestBody MailParam mailParam) {
        try {
            mailService.testConnection();
        } catch (Exception e) {
            String message;
            message = e.getMessage();
            if (message.contains("No provider for")) {
                message = "发送协议配置错误，请检查发送协议";
            } else if (message.contains("Couldn't connect to host")) {
                message = "无法连接至邮件服务器，请检查地址和端口号";
            } else {
                message = "邮箱账号密码验证失败，请检查密码是否应为授权码";
            }
            return BaseResponse.ok(message);
        }
        mailService.sendTextMail(mailParam.getTo(), mailParam.getSubject(), mailParam.getContent());
        return BaseResponse.ok("已发送，请查收。若确认没有收到邮件，请检查服务器日志");
    }

    @PostMapping("test/connection")
    @ApiOperation("Test connection with email server")
    @DisableOnCondition
    public BaseResponse<String> testConnection() {
        mailService.testConnection();
        return BaseResponse.ok("您和邮箱服务器的连接通畅");
    }

}
