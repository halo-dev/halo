package run.halo.app.repository.base;

import org.springframework.data.repository.NoRepositoryBean;
import run.halo.app.model.entity.BaseContent;

/**
 * Base content repository.
 *
 * @author guqing
 * @date 2021-12-18
 */
@NoRepositoryBean
public interface BaseContentRepository<CONTENT extends BaseContent>
    extends BaseRepository<CONTENT, Integer> {

}
