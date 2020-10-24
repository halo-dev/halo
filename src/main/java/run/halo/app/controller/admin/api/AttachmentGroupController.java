package run.halo.app.controller.admin.api;

import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import run.halo.app.model.dto.AttachmentGroupDTO;
import run.halo.app.model.entity.AttachmentGroup;
import run.halo.app.model.params.AttachmentGroupParam;
import run.halo.app.service.AttachmentGroupService;

import javax.validation.Valid;

/**
 * Attachment group controller.
 *
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

    @PostMapping
    @ApiOperation("Creates a attachment group")
    public AttachmentGroupDTO createBy(@RequestBody @Valid AttachmentGroupParam attachmentGroupParam) {
        AttachmentGroup attachmentGroup = attachmentGroupParam.convertTo();
        attachmentGroupService.create(attachmentGroup);
        return new AttachmentGroupDTO().convertFrom(attachmentGroup);
    }
}
