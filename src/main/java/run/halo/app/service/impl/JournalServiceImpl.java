package run.halo.app.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import run.halo.app.model.dto.JournalDTO;
import run.halo.app.model.dto.JournalWithCmtCountDTO;
import run.halo.app.model.entity.Journal;
import run.halo.app.model.params.JournalParam;
import run.halo.app.repository.JournalRepository;
import run.halo.app.service.JournalCommentService;
import run.halo.app.service.JournalService;
import run.halo.app.service.base.AbstractCrudService;
import run.halo.app.utils.ServiceUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Journal service implementation.
 *
 * @author johnniang
 * @date 19-4-24
 */
@Service
public class JournalServiceImpl extends AbstractCrudService<Journal, Integer> implements JournalService {

    private final JournalRepository journalRepository;

    private final JournalCommentService journalCommentService;

    public JournalServiceImpl(JournalRepository journalRepository,
                              JournalCommentService journalCommentService) {
        super(journalRepository);
        this.journalRepository = journalRepository;
        this.journalCommentService = journalCommentService;
    }

    @Override
    public Journal createBy(JournalParam journalParam) {
        Assert.notNull(journalParam, "Journal param must not be null");

        return create(journalParam.convertTo());
    }

    @Override
    public Page<Journal> pageLatest(int top) {
        return listAll(ServiceUtils.buildLatestPageable(top));
    }

    @Override
    public JournalDTO convertTo(Journal journal) {
        Assert.notNull(journal, "Journal must not be null");

        return new JournalDTO().convertFrom(journal);
    }

    @Override
    public List<JournalWithCmtCountDTO> convertToCmtCountDto(List<Journal> journals) {
        if (CollectionUtils.isEmpty(journals)) {
            return Collections.emptyList();
        }

        // Get journal ids
        Set<Integer> journalIds = ServiceUtils.fetchProperty(journals, Journal::getId);

        // Get comment count map
        Map<Integer, Long> journalCommentCountMap = journalCommentService.countByPostIds(journalIds);

        return journals.stream()
                .map(journal -> {
                    JournalWithCmtCountDTO journalWithCmtCountDTO = new JournalWithCmtCountDTO().convertFrom(journal);
                    // Set comment count
                    journalWithCmtCountDTO.setCommentCount(journalCommentCountMap.getOrDefault(journal.getId(), 0L));
                    return journalWithCmtCountDTO;
                })
                .collect(Collectors.toList());
    }

    @Override
    public Page<JournalWithCmtCountDTO> convertToCmtCountDto(Page<Journal> journalPage) {
        Assert.notNull(journalPage, "Journal page must not be null");

        // Convert
        List<JournalWithCmtCountDTO> journalWithCmtCountDTOS = convertToCmtCountDto(journalPage.getContent());

        // Build and return
        return new PageImpl<>(journalWithCmtCountDTOS, journalPage.getPageable(), journalPage.getTotalElements());
    }


}
