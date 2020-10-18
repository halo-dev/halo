package run.halo.app.controller.content.api;

import io.swagger.annotations.ApiOperation;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import run.halo.app.model.dto.EmailDTO;
import run.halo.app.model.dto.LinkDTO;
import run.halo.app.model.vo.EmailVO;
import run.halo.app.model.vo.LinkTeamVO;
import run.halo.app.service.EmailService;
import run.halo.app.service.LinkService;

import java.util.List;

import static org.springframework.data.domain.Sort.Direction.DESC;

/**
 * Content link controller.
 *
 * @author johnniang
 * @author ryanwang
 * @date 2019-04-03
 */
@RestController("ApiContentEmailController")
@RequestMapping("/api/content/emails")
public class EmailController {

    private final EmailService emailService;

    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @GetMapping
    @ApiOperation("List all emails")
    public List<EmailDTO> listEmails(@SortDefault(sort = "createTime", direction = DESC) Sort sort) {
        return emailService.listDTOs(sort);
    }

    @GetMapping("email_view")
    @ApiOperation("Email all emails with team view")
    public List<EmailVO> listEmailVOs(Sort sort) {
        return emailService.listEmailVOs(sort);
    }
}
