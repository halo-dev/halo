package run.halo.app.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;
import run.halo.app.model.entity.Sheet;
import run.halo.app.model.enums.PostStatus;
import run.halo.app.repository.base.BasePostRepository;

import java.util.Optional;

/**
 * Sheet repository.
 *
 * @author johnniang
 * @date 3/22/19
 */
public interface SheetRepository extends BasePostRepository<Sheet> {

    /**
     * Count all sheet visits.
     *
     * @return visits.
     */
    @Override
    @Query("select sum(p.visits) from Sheet p")
    Long countVisit();

    /**
     * Count all sheet likes.
     *
     * @return likes.
     */
    @Override
    @Query("select sum(p.likes) from Sheet p")
    Long countLike();

    /**
     * Gets sheet by url and status.
     *
     * @param url    url must not be blank
     * @param status status must not be null
     * @return an optional of sheet.
     */
    @NonNull
    @Override
    Optional<Sheet> getByUrlAndStatus(@NonNull String url, @NonNull PostStatus status);
}
