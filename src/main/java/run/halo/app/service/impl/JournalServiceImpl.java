package run.halo.app.service.impl;

import cn.hutool.core.lang.Assert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import run.halo.app.model.dto.JournalDTO;
import run.halo.app.model.dto.JournalWithCmtCountDTO;
import run.halo.app.model.entity.Journal;
import run.halo.app.model.params.JournalParam;
import run.halo.app.repository.JournalRepository;
import run.halo.app.service.JournalService;
import run.halo.app.service.base.AbstractCrudService;
import run.halo.app.utils.ServiceUtils;

import java.util.List;

/**
 * Journal service implementation.
 *
 * @author johnniang
 * @date 19-4-24
 */
@Service
public class JournalServiceImpl extends AbstractCrudService<Journal, Integer> implements JournalService {

    private final JournalRepository journalRepository;

    public JournalServiceImpl(JournalRepository journalRepository) {
        super(journalRepository);
        this.journalRepository = journalRepository;
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
    public JournalWithCmtCountDTO convertToCmtCountDto(Journal journal) {

        return null;
    }

    @Override
    public List<JournalWithCmtCountDTO> convertToCmtCountDto(List<Journal> journals) {
        return null;
    }

    @Override
    public Page<JournalWithCmtCountDTO> convertToCmtCountDto(Page<Journal> journalPage) {
        return null;
    }


}
