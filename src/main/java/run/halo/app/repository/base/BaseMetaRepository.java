package run.halo.app.repository.base;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.lang.NonNull;
import run.halo.app.model.entity.BaseMeta;

import java.util.List;

/**
 * Base meta repository.
 *
 * @author ryanwang
 * @author ikaisec
 * @date 2019-08-04
 */
@NoRepositoryBean
public interface BaseMetaRepository<META extends BaseMeta> extends BaseRepository<META, Long>, JpaSpecificationExecutor<META> {

    /**
     * Finds all metas by post id.
     *
     * @param postId post id must not be null
     * @return a list of meta
     */
    @NonNull
    List<META> findAllByPostId(@NonNull Integer postId);
}
