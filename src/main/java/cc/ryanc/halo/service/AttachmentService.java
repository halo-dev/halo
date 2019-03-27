package cc.ryanc.halo.service;

import cc.ryanc.halo.exception.FileUploadException;
import cc.ryanc.halo.model.dto.AttachmentOutputDTO;
import cc.ryanc.halo.model.entity.Attachment;
import cc.ryanc.halo.service.base.CrudService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.web.multipart.MultipartFile;


/**
 * Attachment service.
 *
 * @author johnniang
 */
public interface AttachmentService extends CrudService<Attachment, Integer> {

    /**
     * Pages attachment output dtos.
     *
     * @param pageable page info must not be null
     * @return a page of attachment output dto
     */
    Page<AttachmentOutputDTO> pageDtosBy(Pageable pageable);

    /**
     * Uploads file.
     *
     * @param file multipart file must not be null
     * @return attachment info
     * @throws FileUploadException throws when failed to filehandler the file
     */
    @NonNull
    Attachment upload(@NonNull MultipartFile file);
}
