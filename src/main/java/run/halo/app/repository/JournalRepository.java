package run.halo.app.repository;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import run.halo.app.model.entity.Journal;
import run.halo.app.repository.base.BaseRepository;

/**
 * Journal repository.
 *
 * @author johnniang
 * @date 3/22/19
 */
public interface JournalRepository extends BaseRepository<Journal, Integer>, JpaSpecificationExecutor<Journal> {

}
