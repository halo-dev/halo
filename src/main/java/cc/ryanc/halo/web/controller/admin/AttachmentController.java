package cc.ryanc.halo.web.controller.admin;

import cc.ryanc.halo.service.AttachmentService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Attachments controller
 *
 * @author : RYAN0UP
 * @date : 2019-03-14
 */
@Controller
@RequestMapping(value = "/admin/attachments")
public class AttachmentController {

    private AttachmentService attachmentService;

    public AttachmentController(AttachmentService attachmentService) {
        this.attachmentService = attachmentService;
    }

    /**
     * attachments list
     *
     * @return template path: admin/admin_attachment
     */
    @GetMapping
    public String attachments() {
        return "admin/admin_attachment";
    }
}
