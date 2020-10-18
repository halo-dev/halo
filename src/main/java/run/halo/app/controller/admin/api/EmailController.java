package run.halo.app.controller.admin.api;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import run.halo.app.model.dto.EmailDTO;
import run.halo.app.model.entity.Email;
import run.halo.app.model.params.EmailParam;
import run.halo.app.service.EmailService;
import run.halo.app.service.PostEmailService;

import javax.validation.Valid;
import java.util.List;

/**
 * Email Controller
 *
 * @author ryanwang
 * @date 2019-03-21
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/emails")
public class EmailController {

    private final EmailService emailService;

    private final PostEmailService postEmailService;

    public EmailController(EmailService emailService, PostEmailService postEmailService) {
        this.emailService = emailService;
        this.postEmailService = postEmailService;
    }

    @GetMapping
    @ApiOperation("lists emails")
    public List<? extends EmailDTO> listEmails(@SortDefault(sort = "createTime", direction = Sort.Direction.DESC) Sort sort,
                                                       @ApiParam("Return more information(post count) if it is set")
                                                       @RequestParam(name = "more", required = false, defaultValue = "false") Boolean more) {
        if (more) {
            return postEmailService.listEmailWithCountDTOs(sort);
        }
        return emailService.convertTo(emailService.listAll(sort));
    }

    @PostMapping
    @ApiOperation("Creates a email")
    public EmailDTO createTag(@Valid @RequestBody EmailParam emailParam) {
        // Convert to tag
        Email email = emailParam.convertTo();

        log.debug("Tag to be created: [{}]", email);

        // Create and convert
        return emailService.convertTo(emailService.create(email));
    }
    @GetMapping("{id:\\d+}")
    @ApiOperation("Gets email detail by id")
    public EmailDTO getBy(@PathVariable("id") Integer id) {
        return new EmailDTO().convertFrom(emailService.getById(id));
    }


    @PutMapping("{id:\\d+}")
    @ApiOperation("Updates a email")
    public EmailDTO updateBy(@PathVariable("id") Integer id,
                             @RequestBody @Valid EmailParam emailParam) {
        Email email = emailService.updateBy(id, emailParam);
        return new EmailDTO().convertFrom(email);
    }

    @DeleteMapping("{id:\\d+}")
    @ApiOperation("Delete email by id")
    public void deletePermanently(@PathVariable("id") Integer id) {
        emailService.deletePermanently(id);
    }
}
