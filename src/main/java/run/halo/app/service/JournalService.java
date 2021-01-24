package run.halo.app.service;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import run.halo.app.model.dto.JournalDTO;
import run.halo.app.model.dto.JournalWithCmtCountDTO;
import run.halo.app.model.entity.Journal;
import run.halo.app.model.enums.JournalType;
import run.halo.app.model.params.JournalParam;
import run.halo.app.model.params.JournalQuery;
import run.halo.app.service.base.CrudService;

/**
 * Journal service interface.
 *
 * @author johnniang
 * @author ryanwang
 * @date 2019-04-24
 */
public interface JournalService extends CrudService<Journal, Integer> {

    /**
     * Creates a journal.
     *
     * @param journalParam journal param must not be null
     * @return created journal
     */
    @NonNull
    Journal createBy(@NonNull JournalParam journalParam);

    /**
     * Updates a journal.
     *
     * @param journal journal must not be null
     * @return updated journal
     */
    Journal updateBy(@NonNull Journal journal);

    /**
     * Gets latest journals.
     *
     * @param top max size
     * @return latest journal page
     */
    Page<Journal> pageLatest(int top);

    /**
     * Pages journals.
     *
     * @param journalQuery journal query must not be null
     * @param pageable page info must not be null
     * @return a page of journal
     */
    @NonNull
    Page<Journal> pageBy(@NonNull JournalQuery journalQuery, @NonNull Pageable pageable);

    /**
     * Lists by type.
     *
     * @param type journal type must not be null
     * @param pageable page info must not be null
     * @return a page of journal
     */
    @NonNull
    Page<Journal> pageBy(@NonNull JournalType type, @NonNull Pageable pageable);

    /**
     * Converts to journal dto.
     *
     * @param journal journal must not be null
     * @return journal dto
     */
    @NonNull
    JournalDTO convertTo(@NonNull Journal journal);

    /**
     * Converts to journal with comment count dto list.
     *
     * @param journals journal list
     * @return journal with comment count dto list
     */
    @NonNull
    List<JournalWithCmtCountDTO> convertToCmtCountDto(@Nullable List<Journal> journals);

    /**
     * Converts to journal with comment count dto page.
     *
     * @param journalPage journal page must not be null
     * @return a page of journal with comment count dto
     */
    @NonNull
    Page<JournalWithCmtCountDTO> convertToCmtCountDto(@NonNull Page<Journal> journalPage);

    /**
     * Increases journal likes(1).
     *
     * @param id id must not be null
     */
    void increaseLike(@NonNull Integer id);

    /**
     * Increase journal likes.
     *
     * @param likes likes must not be less than 1
     * @param id id must not be null
     */
    void increaseLike(long likes, @NonNull Integer id);
}
