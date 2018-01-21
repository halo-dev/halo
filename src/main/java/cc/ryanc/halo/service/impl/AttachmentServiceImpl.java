package cc.ryanc.halo.service.impl;

import cc.ryanc.halo.model.domain.Attachment;
import cc.ryanc.halo.repository.AttachmentRepository;
import cc.ryanc.halo.service.AttachmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author : RYAN0UP
 * @version : 1.0
 * description :
 * @date : 2018/1/10
 */
@Service
public class AttachmentServiceImpl implements AttachmentService{

    @Autowired
    private AttachmentRepository attachmentRepository;

    /**
     * 新增附件信息
     * @param attachment attachment
     * @return Attachment
     */
    @Override
    public Attachment saveByAttachment(Attachment attachment) {
        return attachmentRepository.save(attachment);
    }

    @Override
    public List<Attachment> findAllAttachments() {
        return attachmentRepository.findAll();
    }

    @Override
    public Page<Attachment> findAllAttachments(Pageable pageable) {
        return attachmentRepository.findAll(pageable);
    }

    @Override
    public Attachment findByAttachId(Integer attachId) {
        return attachmentRepository.findOne(attachId);
    }

    @Override
    public Attachment removeByAttachId(Integer attachId) {
        Attachment attachment = this.findByAttachId(attachId);
        attachmentRepository.delete(attachment);
        return attachment;
    }
}
