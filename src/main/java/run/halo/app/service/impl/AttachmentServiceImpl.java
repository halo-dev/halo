package run.halo.app.service.impl;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriUtils;
import run.halo.app.exception.AlreadyExistsException;
import run.halo.app.handler.file.FileHandlers;
import run.halo.app.model.dto.AttachmentDTO;
import run.halo.app.model.entity.Attachment;
import run.halo.app.model.enums.AttachmentType;
import run.halo.app.model.params.AttachmentQuery;
import run.halo.app.model.properties.AttachmentProperties;
import run.halo.app.model.support.UploadResult;
import run.halo.app.repository.AttachmentRepository;
import run.halo.app.service.AttachmentService;
import run.halo.app.service.OptionService;
import run.halo.app.service.base.AbstractCrudService;
import run.halo.app.utils.FilenameUtils;
import run.halo.app.utils.HaloUtils;

/**
 * AttachmentService implementation
 *
 * @author ryanwang
 * @author johnniang
 * @date 2019-03-14
 */
@Slf4j
@Service
public class AttachmentServiceImpl extends AbstractCrudService<Attachment, Integer>
    implements AttachmentService {

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
    public Page<AttachmentDTO> pageDtosBy(@NonNull Pageable pageable,
        AttachmentQuery attachmentQuery) {
        Assert.notNull(pageable, "Page info must not be null");

        // List all
        Page<Attachment> attachmentPage =
            attachmentRepository.findAll(buildSpecByQuery(attachmentQuery), pageable);

        // Convert and return
        return attachmentPage.map(this::convertToDto);
    }

    @NonNull
    private Specification<Attachment> buildSpecByQuery(@NonNull AttachmentQuery attachmentQuery) {
        Assert.notNull(attachmentQuery, "Attachment query must not be null");

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new LinkedList<>();

            if (attachmentQuery.getMediaType() != null) {
                predicates.add(
                    criteriaBuilder.equal(root.get("mediaType"), attachmentQuery.getMediaType()));
            }

            if (attachmentQuery.getAttachmentType() != null) {
                predicates.add(
                    criteriaBuilder.equal(root.get("type"), attachmentQuery.getAttachmentType()));
            }

            if (attachmentQuery.getKeyword() != null) {

                String likeCondition =
                    String.format("%%%s%%", StringUtils.strip(attachmentQuery.getKeyword()));

                Predicate nameLike = criteriaBuilder.like(root.get("name"), likeCondition);

                predicates.add(criteriaBuilder.or(nameLike));
            }

            return query.where(predicates.toArray(new Predicate[0])).getRestriction();
        };
    }

    @Override
    public Attachment upload(MultipartFile file) {
        Assert.notNull(file, "Multipart file must not be null");

        AttachmentType attachmentType = getAttachmentType();

        log.debug("Starting uploading... type: [{}], file: [{}]", attachmentType,
            file.getOriginalFilename());

        // Upload file
        UploadResult uploadResult = fileHandlers.upload(file, attachmentType);

        log.debug("Attachment type: [{}]", attachmentType);
        log.debug("Upload result: [{}]", uploadResult);

        // Build attachment
        Attachment attachment = new Attachment();
        attachment.setName(uploadResult.getFilename());
        // Convert separator
        attachment.setPath(HaloUtils.changeFileSeparatorToUrlSeparator(uploadResult.getFilePath()));
        attachment.setFileKey(uploadResult.getKey());
        attachment.setThumbPath(
            HaloUtils.changeFileSeparatorToUrlSeparator(uploadResult.getThumbPath()));
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

    @Override
    public List<Attachment> removePermanently(@Nullable Collection<Integer> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyList();
        }

        return ids.stream().map(this::removePermanently).collect(Collectors.toList());
    }

    @Override
    public AttachmentDTO convertToDto(Attachment attachment) {
        Assert.notNull(attachment, "Attachment must not be null");

        // Get blog base url
        String blogBaseUrl = optionService.getBlogBaseUrl();

        Boolean enabledAbsolutePath = optionService.isEnabledAbsolutePath();

        // Convert to output dto
        AttachmentDTO attachmentDTO = new AttachmentDTO().convertFrom(attachment);

        if (Objects.equals(attachmentDTO.getType(), AttachmentType.LOCAL)) {

            // 将 local 存储的链接中的文件名替换为编码后的文件名
            String path = attachmentDTO.getPath()
                .replace(attachmentDTO.getName(), encodeValue(attachmentDTO.getName()));

            String basename = FilenameUtils.getBasename(attachmentDTO.getName());
            String extension = FilenameUtils.getExtension(attachmentDTO.getName());
            // 得到 thumbnail name
            String thumbnailName = String.format("%s-thumbnail%s", basename, extension);
            String thumbnailPath = attachmentDTO.getThumbPath()
                .replace(thumbnailName, encodeValue(thumbnailName));

            // Append blog base url to path and thumbnail
            String fullPath = StringUtils
                .join(enabledAbsolutePath ? blogBaseUrl : "", "/", path);
            String fullThumbPath = StringUtils
                .join(enabledAbsolutePath ? blogBaseUrl : "", "/", thumbnailPath);

            // 对于之前上传的链接，需要将文件名替换为编码后的文件名
            fullThumbPath = HaloUtils.changeFileSeparatorToUrlSeparator(fullThumbPath);

            // Set full path and full thumb path
            attachmentDTO.setPath(fullPath);
            attachmentDTO.setThumbPath(fullThumbPath);
        }

        return attachmentDTO;
    }

    private String encodeValue(String value) {
        return UriUtils.encode(value, StandardCharsets.UTF_8);
    }


    @Override
    public List<String> listAllMediaType() {
        return attachmentRepository.findAllMediaType();
    }

    @Override
    public List<AttachmentType> listAllType() {
        return attachmentRepository.findAllType();
    }

    @Override
    public Attachment create(Attachment attachment) {
        Assert.notNull(attachment, "Attachment must not be null");

        // Check attachment path
        pathMustNotExist(attachment);

        return super.create(attachment);
    }

    /**
     * Attachment path must not be exist.
     *
     * @param attachment attachment must not be null
     */
    private void pathMustNotExist(@NonNull Attachment attachment) {
        Assert.notNull(attachment, "Attachment must not be null");

        long pathCount = attachmentRepository.countByPath(attachment.getPath());

        if (pathCount > 0) {
            throw new AlreadyExistsException("附件路径为 " + attachment.getPath() + " 已经存在");
        }
    }

    /**
     * Get attachment type from options.
     *
     * @return attachment type
     */
    @NonNull
    private AttachmentType getAttachmentType() {
        return Objects.requireNonNull(optionService
            .getEnumByPropertyOrDefault(AttachmentProperties.ATTACHMENT_TYPE, AttachmentType.class,
                AttachmentType.LOCAL));
    }
}
