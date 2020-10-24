package run.halo.app.service;

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
}