package run.halo.app.controller.admin.api;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import run.halo.app.model.params.MailParam;
import run.halo.app.model.support.BaseResponse;
import run.halo.app.service.MailService;

import javax.validation.Valid;

/**
 * Mail controller.
 *
 * @author johnniang
 * @date 19-5-7
 */
@RestController
@RequestMapping("/api/admin/mails")
public class MailController {

    private final MailService mailService;

    public MailController(MailService mailService) {
        this.mailService = mailService;
    }

    @PostMapping("test")
    public BaseResponse testMail(@Valid @RequestBody MailParam mailParam) {
        mailService.sendMail(mailParam.getTo(), mailParam.getSubject(), mailParam.getContent());
        return BaseResponse.ok("发送成功");
    }
}
