package cc.ryanc.halo.service.impl;

import cc.ryanc.halo.model.entity.Attachment;
import cc.ryanc.halo.repository.AttachmentRepository;
import cc.ryanc.halo.service.AttachmentService;
import cc.ryanc.halo.service.base.AbstractCrudService;
import org.springframework.stereotype.Service;

/**
 * AttachmentService implementation class
 *
 * @author : RYAN0UP
 * @date : 2019-03-14
 */
@Service
public class AttachmentServiceImpl extends AbstractCrudService<Attachment, Integer> implements AttachmentService {

    private AttachmentRepository attachmentRepository;

    public AttachmentServiceImpl(AttachmentRepository attachmentRepository) {
        super(attachmentRepository);
        this.attachmentRepository = attachmentRepository;
    }
}
