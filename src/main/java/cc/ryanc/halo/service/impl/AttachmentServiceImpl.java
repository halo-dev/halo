package cc.ryanc.halo.service.impl;

import cc.ryanc.halo.model.domain.Attachment;
import cc.ryanc.halo.repository.AttachmentRepository;
import cc.ryanc.halo.service.AttachmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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

    private static final String CATEGORY_KEY = "'category_key'";

    private static final String CATEGORY_CACHE_NAME = "cateCache";

    /**
     * 新增附件信息
     *
     * @param attachment attachment
     * @return Attachment
     */
    @Override
    public Attachment saveByAttachment(Attachment attachment) {
        return attachmentRepository.save(attachment);
    }

    /**
     * 获取所有附件信息
     *
     * @return list
     */
    @Override
    public List<Attachment> findAllAttachments() {
        return attachmentRepository.findAll();
    }

    /**
     * 获取所有附件信息 分页
     *
     * @param pageable pageable
     * @return page
     */
    @Override
    public Page<Attachment> findAllAttachments(Pageable pageable) {
        return attachmentRepository.findAll(pageable);
    }

    /**
     * 根据附件id查询附件
     *
     * @param attachId attachId
     * @return attachment
     */
    @Override
    public Optional<Attachment> findByAttachId(Long attachId) {
        return attachmentRepository.findById(attachId);
    }

    /**
     * 根据编号移除附件
     *
     * @param attachId attachId
     * @return attachment
     */
    @Override
    public Optional<Attachment> removeByAttachId(Long attachId) {
        Optional<Attachment> attachment = this.findByAttachId(attachId);
        attachmentRepository.delete(attachment.get());
        return attachment;
    }
}
