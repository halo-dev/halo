package run.halo.app.service.impl;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import run.halo.app.exception.NotFoundException;
import run.halo.app.model.entity.JournalComment;
import run.halo.app.repository.JournalCommentRepository;
import run.halo.app.repository.JournalRepository;
import run.halo.app.service.JournalCommentService;
import run.halo.app.service.OptionService;
import run.halo.app.service.UserService;
import run.halo.app.service.assembler.comment.JournalCommentAssembler;

/**
 * Journal comment service implementation.
 *
 * @author johnniang
 * @author guqing
 * @date 2019-04-25
 */
@Service
public class JournalCommentServiceImpl extends BaseCommentServiceImpl<JournalComment>
    implements JournalCommentService {

    private final JournalRepository journalRepository;

    public JournalCommentServiceImpl(JournalCommentRepository journalCommentRepository,
        OptionService optionService,
        UserService userService,
        ApplicationEventPublisher eventPublisher,
        JournalRepository journalRepository,
        JournalCommentAssembler journalCommentAssembler) {
        super(journalCommentRepository, optionService, userService, eventPublisher,
            journalCommentAssembler);
        this.journalRepository = journalRepository;
    }

    @Override
    public void validateTarget(@NonNull Integer journalId) {
        if (!journalRepository.existsById(journalId)) {
            throw new NotFoundException("查询不到该日志信息").setErrorData(journalId);
        }
    }
}
