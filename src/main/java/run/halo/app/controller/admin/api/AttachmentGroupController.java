package run.halo.app.controller.admin.api;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import run.halo.app.service.AttachmentGroupService;

/**
 * @author guqing
 * @date 2020-10-24
 */
@RestController
@RequestMapping("/api/admin/attachments/groups")
public class AttachmentGroupController {

    private final AttachmentGroupService attachmentGroupService;

    public AttachmentGroupController(AttachmentGroupService attachmentGroupService) {
        this.attachmentGroupService = attachmentGroupService;
    }
}
