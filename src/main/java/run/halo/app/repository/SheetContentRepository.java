package run.halo.app.repository;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import run.halo.app.model.entity.SheetContent;
import run.halo.app.repository.base.BaseContentRepository;

/**
 * Sheet content repository.
 *
 * @author guqing
 * @date 2022-01-07
 */
public interface SheetContentRepository extends BaseContentRepository<SheetContent>,
    JpaSpecificationExecutor<SheetContent> {

}
