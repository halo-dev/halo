package run.halo.app.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import run.halo.app.model.entity.Journal;
import run.halo.app.model.enums.JournalType;
import run.halo.app.repository.base.BaseRepository;

/**
 * Journal repository.
 *
 * @author johnniang
 * @author ryanwang
 * @date 2019-03-22
 */
public interface JournalRepository
    extends BaseRepository<Journal, Integer>, JpaSpecificationExecutor<Journal> {

    /**
     * Finds journals by type and pageable.
     *
     * @param type journal type must not be null
     * @param pageable page info must not be null
     * @return a page of journal
     */
    @NonNull
    Page<Journal> findAllByType(@NonNull JournalType type, @NonNull Pageable pageable);

    /**
     * Updates journal likes.
     *
     * @param likes likes delta
     * @param id id must not be null
     * @return updated rows
     */
    @Modifying
    @Query("update Journal j set j.likes = j.likes + :likes where j.id = :id")
    int updateLikes(@Param("likes") long likes, @Param("id") @NonNull Integer id);
}
