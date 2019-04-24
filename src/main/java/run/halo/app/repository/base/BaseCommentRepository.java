package run.halo.app.repository.base;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import run.halo.app.model.entity.BaseComment;
import run.halo.app.model.enums.CommentStatus;

/**
 * Base comment repository.
 *
 * @author johnniang
 * @date 19-4-24
 */
public interface BaseCommentRepository<DOMAIN extends BaseComment> extends BaseRepository<DOMAIN, Long>, JpaSpecificationExecutor<DOMAIN> {

    /**
     * Finds all comments by status.
     *
     * @param status   status must not be null
     * @param pageable page info must not be null
     * @return a page of comment
     */
    @NonNull
    Page<DOMAIN> findAllByStatus(@Nullable CommentStatus status, @NonNull Pageable pageable);


}
