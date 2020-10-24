package run.halo.app.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import run.halo.app.model.dto.AttachmentViewDTO;
import run.halo.app.model.entity.AttachmentGroup;
import run.halo.app.model.params.AttachmentQuery;
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
     * list attachments and groups output dto.
     *
     * @param groupId         attachment group id
     * @return a list of attachments and attachment groups output dto
     */
    @NonNull
    AttachmentViewDTO listBy(@NonNull Integer groupId);
}