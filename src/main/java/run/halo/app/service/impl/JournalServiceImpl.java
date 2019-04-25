package run.halo.app.service.impl;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import run.halo.app.model.entity.Journal;
import run.halo.app.repository.JournalRepository;
import run.halo.app.repository.PostRepository;
import run.halo.app.service.JournalService;
import run.halo.app.service.OptionService;

/**
 * Journal service implementation.
 *
 * @author johnniang
 * @date 19-4-24
 */
@Service
public class JournalServiceImpl extends BaseCommentServiceImpl<Journal> implements JournalService {

    private final JournalRepository journalRepository;

    public JournalServiceImpl(JournalRepository journalRepository,
                              PostRepository postRepository,
                              OptionService optionService,
                              ApplicationEventPublisher eventPublisher) {
        super(journalRepository, postRepository, optionService, eventPublisher);
        this.journalRepository = journalRepository;
    }
}
