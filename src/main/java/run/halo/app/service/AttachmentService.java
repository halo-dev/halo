package run.halo.app.service;

import run.halo.app.exception.FileOperationException;
import run.halo.app.model.dto.AttachmentOutputDTO;
import run.halo.app.model.entity.Attachment;
import run.halo.app.service.base.CrudService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.web.multipart.MultipartFile;
import run.halo.app.service.base.CrudService;


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
    @NonNull
    Page<AttachmentOutputDTO> pageDtosBy(@NonNull Pageable pageable);

    /**
     * Uploads file.
     *
     * @param file multipart file must not be null
     * @return attachment info
     * @throws FileOperationException throws when failed to filehandler the file
     */
    @NonNull
    Attachment upload(@NonNull MultipartFile file);

    /**
     * Removes attachment permanently.
     *
     * @param id attachment id must not be null
     * @return attachment detail deleted
     */
    @NonNull
    Attachment removePermanently(@NonNull Integer id);
}
