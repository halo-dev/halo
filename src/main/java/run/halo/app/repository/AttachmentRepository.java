package run.halo.app.repository;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import run.halo.app.model.entity.Attachment;
import run.halo.app.repository.base.BaseRepository;

/**
 * Attachment repository
 *
 * @author johnniang
 */
public interface AttachmentRepository extends BaseRepository<Attachment, Integer>, JpaSpecificationExecutor<Attachment> {
}
