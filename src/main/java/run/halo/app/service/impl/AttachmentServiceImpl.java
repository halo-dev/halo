package run.halo.app.service.impl;

import run.halo.app.handler.file.FileHandlers;
import run.halo.app.model.dto.AttachmentOutputDTO;
import run.halo.app.model.entity.Attachment;
import run.halo.app.model.enums.AttachmentType;
import run.halo.app.model.properties.AttachmentProperties;
import run.halo.app.model.support.UploadResult;
import run.halo.app.repository.AttachmentRepository;
import run.halo.app.service.AttachmentService;
import run.halo.app.service.OptionService;
import run.halo.app.service.base.AbstractCrudService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;
import run.halo.app.handler.file.FileHandlers;
import run.halo.app.service.base.AbstractCrudService;

/**
 * AttachmentService implementation class
 *
 * @author : RYAN0UP
 * @date : 2019-03-14
 */
@Slf4j
@Service
public class AttachmentServiceImpl extends AbstractCrudService<Attachment, Integer> implements AttachmentService {

    private final AttachmentRepository attachmentRepository;

    private final OptionService optionService;

    private final FileHandlers fileHandlers;

    public AttachmentServiceImpl(AttachmentRepository attachmentRepository,
                                 OptionService optionService,
                                 FileHandlers fileHandlers) {
        super(attachmentRepository);
        this.attachmentRepository = attachmentRepository;
        this.optionService = optionService;
        this.fileHandlers = fileHandlers;
    }

    @Override
    public Page<AttachmentOutputDTO> pageDtosBy(Pageable pageable) {
        Assert.notNull(pageable, "Page info must not be null");

        // List all
        Page<Attachment> attachmentPage = listAll(pageable);

        // Convert and return
        return attachmentPage.map(attachment -> new AttachmentOutputDTO().convertFrom(attachment));
    }

    @Override
    public Attachment upload(MultipartFile file) {
        Assert.notNull(file, "Multipart file must not be null");

        AttachmentType attachmentType = getAttachmentType();

        log.debug("Starting uploading... type: [{}], file: [{}]", attachmentType, file.getOriginalFilename());

        // Upload file
        UploadResult uploadResult = fileHandlers.upload(file, attachmentType);

        log.debug("Attachment type: [{}]", attachmentType);
        log.debug("Upload result: [{}]", uploadResult);

        // Build attachment
        Attachment attachment = new Attachment();
        attachment.setName(uploadResult.getFilename());
        attachment.setPath(uploadResult.getFilePath());
        attachment.setFileKey(uploadResult.getKey());
        attachment.setThumbPath(uploadResult.getThumbPath());
        attachment.setMediaType(uploadResult.getMediaType().toString());
        attachment.setSuffix(uploadResult.getSuffix());
        attachment.setWidth(uploadResult.getWidth());
        attachment.setHeight(uploadResult.getHeight());
        attachment.setSize(uploadResult.getSize());
        attachment.setType(attachmentType);

        log.debug("Creating attachment: [{}]", attachment);

        // Create and return
        return create(attachment);
    }

    @Override
    public Attachment removePermanently(Integer id) {
        // Remove it from database
        Attachment deletedAttachment = removeById(id);

        // Remove the file
        fileHandlers.delete(deletedAttachment);

        log.debug("Deleted attachment: [{}]", deletedAttachment);

        return deletedAttachment;
    }

    /**
     * Get attachment type from options.
     *
     * @return attachment type
     */
    @NonNull
    private AttachmentType getAttachmentType() {
        return optionService.getEnumByPropertyOrDefault(AttachmentProperties.ATTACHMENT_TYPE, AttachmentType.class, AttachmentType.LOCAL);
    }
}
