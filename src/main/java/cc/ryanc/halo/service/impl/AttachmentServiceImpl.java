package cc.ryanc.halo.service.impl;

import cc.ryanc.halo.model.dto.AttachmentOutputDTO;
import cc.ryanc.halo.model.entity.Attachment;
import cc.ryanc.halo.repository.AttachmentRepository;
import cc.ryanc.halo.service.AttachmentService;
import cc.ryanc.halo.service.base.AbstractCrudService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * AttachmentService implementation class
 *
 * @author : RYAN0UP
 * @date : 2019-03-14
 */
@Service
public class AttachmentServiceImpl extends AbstractCrudService<Attachment, Integer> implements AttachmentService {

    private final AttachmentRepository attachmentRepository;

    public AttachmentServiceImpl(AttachmentRepository attachmentRepository) {
        super(attachmentRepository);
        this.attachmentRepository = attachmentRepository;
    }

    @Override
    public Page<AttachmentOutputDTO> pageDtosBy(Pageable pageable) {
        Assert.notNull(pageable, "Page info must not be null");

        // List all
        Page<Attachment> attachmentPage = listAll(pageable);

        // Convert and return
        return attachmentPage.map(attachment -> new AttachmentOutputDTO().convertFrom(attachment));
    }
}
