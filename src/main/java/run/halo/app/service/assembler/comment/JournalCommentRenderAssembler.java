package run.halo.app.service.assembler.comment;

import java.util.Comparator;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import run.halo.app.model.dto.BaseCommentDTO;
import run.halo.app.model.entity.JournalComment;
import run.halo.app.model.vo.BaseCommentVO;
import run.halo.app.model.vo.JournalCommentWithJournalVO;
import run.halo.app.repository.JournalRepository;
import run.halo.app.service.OptionService;

/**
 * Journal comment assembler for theme render.
 *
 * @author guqing
 * @date 2022-03-09
 */
@Component
public class JournalCommentRenderAssembler extends JournalCommentAssembler {
    public JournalCommentRenderAssembler(OptionService optionService,
        JournalRepository journalRepository) {
        super(optionService, journalRepository);
    }

    @NonNull
    @Override
    public BaseCommentDTO convertTo(@NonNull JournalComment comment) {
        clearSensitiveField(comment);
        return super.convertTo(comment);
    }

    @NonNull
    @Override
    public List<BaseCommentDTO> convertTo(List<JournalComment> journalComments) {
        journalComments.forEach(this::clearSensitiveField);
        return super.convertTo(journalComments);
    }

    @NonNull
    @Override
    public Page<BaseCommentDTO> convertTo(Page<JournalComment> journalComments) {
        journalComments.getContent().forEach(this::clearSensitiveField);
        return super.convertTo(journalComments);
    }

    @Override
    public List<BaseCommentVO> convertToVo(List<JournalComment> journalComments,
        Comparator<BaseCommentVO> comparator) {
        if (!CollectionUtils.isEmpty(journalComments)) {
            journalComments.forEach(this::clearSensitiveField);
        }
        return super.convertToVo(journalComments, comparator);
    }

    @NonNull
    @Override
    public List<JournalCommentWithJournalVO> convertToWithJournalVo(
        List<JournalComment> journalComments) {
        if (!CollectionUtils.isEmpty(journalComments)) {
            journalComments.forEach(this::clearSensitiveField);
        }
        return super.convertToWithJournalVo(journalComments);
    }

    @NonNull
    @Override
    public Page<JournalCommentWithJournalVO> convertToWithJournalVo(
        @NonNull Page<JournalComment> journalCommentPage) {
        journalCommentPage.getContent().forEach(this::clearSensitiveField);
        return super.convertToWithJournalVo(journalCommentPage);
    }

    @NonNull
    @Override
    public Page<BaseCommentVO> pageVosBy(@NonNull List<JournalComment> journalComments,
        @NonNull Pageable pageable) {
        journalComments.forEach(this::clearSensitiveField);
        return super.pageVosBy(journalComments, pageable);
    }
}
