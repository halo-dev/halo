package run.halo.app.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Assert;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import run.halo.app.exception.NotFoundException;
import run.halo.app.model.dto.JournalDTO;
import run.halo.app.model.entity.Journal;
import run.halo.app.model.entity.JournalComment;
import run.halo.app.model.vo.JournalCommentWithJournalVO;
import run.halo.app.repository.JournalCommentRepository;
import run.halo.app.repository.JournalRepository;
import run.halo.app.service.JournalCommentService;
import run.halo.app.service.OptionService;
import run.halo.app.utils.ServiceUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Journal comment service implementation.
 *
 * @author johnniang
 * @date 19-4-25
 */
@Service
public class JournalCommentServiceImpl extends BaseCommentServiceImpl<JournalComment> implements JournalCommentService {

    private final JournalCommentRepository journalCommentRepository;

    private final JournalRepository journalRepository;

    public JournalCommentServiceImpl(JournalCommentRepository journalCommentRepository,
                                     OptionService optionService,
                                     ApplicationEventPublisher eventPublisher, JournalRepository journalRepository) {
        super(journalCommentRepository, optionService, eventPublisher);
        this.journalCommentRepository = journalCommentRepository;
        this.journalRepository = journalRepository;
    }

    @Override
    public void targetMustExist(Integer journalId) {
        if (!journalRepository.existsById(journalId)) {
            throw new NotFoundException("The journal with id " + journalId + " was not found");
        }
    }

    @Override
    public List<JournalCommentWithJournalVO> convertToWithJournalVo(List<JournalComment> journalComments) {

        if (CollectionUtil.isEmpty(journalComments)) {
            return Collections.emptyList();
        }

        Set<Integer> journalIds = ServiceUtils.fetchProperty(journalComments, JournalComment::getPostId);

        // Get all journals
        List<Journal> journals = journalRepository.findAllById(journalIds);

        Map<Integer, Journal> journalMap = ServiceUtils.convertToMap(journals, Journal::getId);

        return journalComments.stream()
                .filter(journalComment -> journalMap.containsKey(journalComment.getPostId()))
                .map(journalComment -> {
                    JournalCommentWithJournalVO journalCmtWithJournalVo = new JournalCommentWithJournalVO().convertFrom(journalComment);
                    journalCmtWithJournalVo.setJournal(new JournalDTO().convertFrom(journalMap.get(journalComment.getPostId())));
                    return journalCmtWithJournalVo;
                })
                .collect(Collectors.toList());
    }

    @Override
    public Page<JournalCommentWithJournalVO> convertToWithJournalVo(Page<JournalComment> journalCommentPage) {
        Assert.notNull(journalCommentPage, "Journal comment page must not be null");

        // Convert the list
        List<JournalCommentWithJournalVO> journalCmtWithJournalVOS = convertToWithJournalVo(journalCommentPage.getContent());

        // Build and return
        return new PageImpl<>(journalCmtWithJournalVOS, journalCommentPage.getPageable(), journalCommentPage.getTotalElements());
    }
}
