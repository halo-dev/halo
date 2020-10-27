package run.halo.app.service;

import org.springframework.lang.NonNull;
import run.halo.app.model.dto.AttachmentViewDTO;
import run.halo.app.model.entity.AttachmentGroup;
import run.halo.app.service.base.CrudService;

import java.util.List;

/**
 * Attachment group service.
 *
 * @author guqing
 * @date 2020-10-24
 */
public interface AttachmentGroupService extends CrudService<AttachmentGroup, Integer> {
    /**
     * Recursively delete groups and attachments.
     *
     * @param groupIds a collection of attachment group id is used as parent id
     */
    void removeGroupAndAttachmentBy(List<Integer> groupIds);

    /**
     * List attachments and groups output dto.
     *
     * @param groupId attachment group id
     * @return a list of attachments and attachment groups output dto
     */
    @NonNull
    AttachmentViewDTO listBy(@NonNull Integer groupId);

    /**
     * Create attachment.
     * Fast return if a group with the same name exists at the same level
     *
     * @param attachmentGroup attachment group param
     * @return persistent attachment group object
     */
    AttachmentGroup createBy(AttachmentGroup attachmentGroup);
}