package run.halo.app.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import run.halo.app.model.entity.AttachmentGroup;
import run.halo.app.repository.AttachmentGroupRepository;
import run.halo.app.service.AttachmentGroupService;
import run.halo.app.service.base.AbstractCrudService;

/**
 * Attachment group service implementation.
 *
 * @author guqing
 * @date 2020-10-24
 */
@Slf4j
@Service
public class AttachmentGroupServiceImpl extends AbstractCrudService<AttachmentGroup, Integer> implements AttachmentGroupService {

    private final AttachmentGroupRepository attachmentGroupRepository;

    public AttachmentGroupServiceImpl(AttachmentGroupRepository attachmentGroupRepository) {
        super(attachmentGroupRepository);
        this.attachmentGroupRepository = attachmentGroupRepository;
    }
}
