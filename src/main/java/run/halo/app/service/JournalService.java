package run.halo.app.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import run.halo.app.model.entity.Journal;
import run.halo.app.model.params.JournalParam;
import run.halo.app.service.base.BaseCommentService;

/**
 * Journal service interface.
 *
 * @author johnniang
 * @date 19-4-24
 */
public interface JournalService extends BaseCommentService<Journal> {

    /**
     * Creates a journal.
     *
     * @param journalParam journal param must not be null
     * @return created journal
     */
    @NonNull
    Journal createBy(@NonNull JournalParam journalParam);

    /**
     * Gets a page of journal
     *
     * @param pageable page info must not be null
     * @return a page of journal
     */
    Page<Journal> pageBy(@NonNull Pageable pageable);
}
