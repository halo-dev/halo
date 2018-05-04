package cc.ryanc.halo.service;

import cc.ryanc.halo.model.domain.Attachment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * @author : RYAN0UP
 * @version : 1.0
 * @date : 2018/1/10
 */
public interface AttachmentService {

    /**
     * 添加附件信息
     *
     * @param attachment attachment
     * @return Attachment
     */
    Attachment saveByAttachment(Attachment attachment);

    /**
     * 查询所有附件信息
     *
     * @return list
     */
    List<Attachment> findAllAttachments();

    /**
     * 查询所有附件，分页
     *
     * @param pageable pageable
     * @return page
     */
    Page<Attachment> findAllAttachments(Pageable pageable);

    /**
     * 根据编号查询
     *
     * @param attachId attachId
     * @return Attachment
     */
    Optional<Attachment> findByAttachId(Long attachId);

    /**
     * 根据编号移除
     *
     * @param attachId attachId
     * @return Attachment
     */
    Attachment removeByAttachId(Long attachId);
}
