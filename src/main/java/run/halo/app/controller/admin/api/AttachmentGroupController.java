package run.halo.app.controller.admin.api;

import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import run.halo.app.model.dto.AttachmentGroupDTO;
import run.halo.app.model.entity.AttachmentGroup;
import run.halo.app.model.params.AttachmentGroupParam;
import run.halo.app.service.AttachmentGroupService;

import javax.validation.Valid;
import java.util.List;

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

    @PutMapping("{id:\\d+}")
    @ApiOperation("Updates a attachment group")
    public AttachmentGroupDTO updateBy(@PathVariable Integer id,
                                       @RequestBody @Valid AttachmentGroupParam attachmentGroupParam) {
        AttachmentGroup attachmentGroupToUpdate = attachmentGroupService.getById(id);
        attachmentGroupParam.update(attachmentGroupToUpdate);

        attachmentGroupService.update(attachmentGroupToUpdate);

        return new AttachmentGroupDTO().convertFrom(attachmentGroupToUpdate);
    }

    @DeleteMapping
    @ApiOperation("Recursively delete attachment groups and attachments by group ids")
    public void deletePermanently(@RequestBody List<Integer> groupIds) {
        attachmentGroupService.removeGroupAndAttachmentBy(groupIds);
    }
}
