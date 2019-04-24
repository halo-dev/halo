package run.halo.app.service.impl;

import run.halo.app.model.entity.Journal;
import run.halo.app.repository.JournalRepository;
import run.halo.app.service.JournalService;
import run.halo.app.service.base.AbstractCrudService;

/**
 * Journal service implementation.
 *
 * @author johnniang
 * @date 19-4-24
 */
public class JournalServiceImpl extends AbstractCrudService<Journal, Long> implements JournalService {

    private final JournalRepository journalRepository;

    public JournalServiceImpl(JournalRepository journalRepository) {
        super(journalRepository);
        this.journalRepository = journalRepository;
    }
}
