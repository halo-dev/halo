package run.halo.app.service;

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

}
