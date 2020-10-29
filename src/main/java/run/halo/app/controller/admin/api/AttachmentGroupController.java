package run.halo.app.controller.admin.api;

import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import run.halo.app.model.dto.AttachmentGroupDTO;
import run.halo.app.model.dto.AttachmentViewDTO;
import run.halo.app.model.entity.AttachmentGroup;
import run.halo.app.model.params.AttachmentGroupParam;
import run.halo.app.service.AttachmentGroupService;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

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

    @GetMapping("/{groupId:\\d+}")
    @ApiOperation("List all attachment groups")
    public AttachmentViewDTO listBy(@PathVariable Integer groupId) {
        return attachmentGroupService.listBy(groupId);
    }

    @GetMapping("/parent/{parentId:\\d+}")
    public List<AttachmentGroupDTO> listByParent(@PathVariable Integer parentId) {
        List<AttachmentGroup> attachmentGroups = attachmentGroupService.listByParentId(parentId);
        return convertTo(attachmentGroups);
    }

    @PostMapping
    @ApiOperation("Creates a attachment group")
    public AttachmentGroupDTO createBy(@RequestBody @Valid AttachmentGroupParam attachmentGroupParam) {
        AttachmentGroup attachmentGroup = attachmentGroupParam.convertTo();
        attachmentGroupService.createBy(attachmentGroup);
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

    @PutMapping("/move/{parentId:\\d+}")
    public List<AttachmentGroupDTO> moveTo(@PathVariable Integer parentId, @RequestBody List<Integer> ids) {
        List<AttachmentGroup> attachmentGroups = attachmentGroupService.batchMoveTo(ids, parentId);
        return convertTo(attachmentGroups);
    }

    @DeleteMapping
    @ApiOperation("Recursively delete attachment groups and attachments by group ids")
    public void deletePermanently(@RequestBody List<Integer> groupIds) {
        attachmentGroupService.removeGroupAndAttachmentBy(groupIds);
    }

    private List<AttachmentGroupDTO> convertTo(List<AttachmentGroup> attachmentGroups) {
        return attachmentGroups.stream()
                .map(attachmentGroup -> {
                    AttachmentGroupDTO attachmentGroupDTO = new AttachmentGroupDTO();
                    attachmentGroupDTO.convertFrom(attachmentGroup);
                    return attachmentGroupDTO;
                })
                .collect(Collectors.toList());
    }
}
