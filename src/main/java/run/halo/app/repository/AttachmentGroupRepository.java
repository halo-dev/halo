package run.halo.app.repository;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import run.halo.app.model.entity.AttachmentGroup;
import run.halo.app.repository.base.BaseRepository;

/**
 * Attachment group repository
 * @author guqing
 * @date 2020-10-24
 */
public interface AttachmentGroupRepository extends BaseRepository<AttachmentGroup, Integer>, JpaSpecificationExecutor<AttachmentGroup> {
}
