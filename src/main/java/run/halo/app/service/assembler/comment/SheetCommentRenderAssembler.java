package run.halo.app.service.assembler.comment;

import java.util.Comparator;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import run.halo.app.model.dto.BaseCommentDTO;
import run.halo.app.model.entity.SheetComment;
import run.halo.app.model.vo.BaseCommentVO;
import run.halo.app.model.vo.SheetCommentWithSheetVO;
import run.halo.app.repository.SheetRepository;
import run.halo.app.service.OptionService;

/**
 * Sheet comment assembler for theme render.
 *
 * @author guqing
 * @date 2022-03-09
 */
@Component
public class SheetCommentRenderAssembler extends SheetCommentAssembler {

    public SheetCommentRenderAssembler(OptionService optionService,
        SheetRepository sheetRepository) {
        super(optionService, sheetRepository);
    }

    @NonNull
    @Override
    public BaseCommentDTO convertTo(@NonNull SheetComment comment) {
        clearSensitiveField(comment);
        return super.convertTo(comment);
    }

    @NonNull
    @Override
    public List<BaseCommentDTO> convertTo(@NonNull List<SheetComment> sheetComments) {
        sheetComments.forEach(this::clearSensitiveField);
        return super.convertTo(sheetComments);
    }

    @NonNull
    @Override
    public Page<BaseCommentDTO> convertTo(@NonNull Page<SheetComment> sheetComments) {
        sheetComments.getContent().forEach(this::clearSensitiveField);
        return super.convertTo(sheetComments);
    }

    @Override
    public List<BaseCommentVO> convertToVo(List<SheetComment> sheetComments,
        Comparator<BaseCommentVO> comparator) {
        if (!CollectionUtils.isEmpty(sheetComments)) {
            sheetComments.forEach(this::clearSensitiveField);
        }
        return super.convertToVo(sheetComments, comparator);
    }

    @NonNull
    @Override
    public SheetCommentWithSheetVO convertToWithSheetVo(@NonNull SheetComment comment) {
        clearSensitiveField(comment);
        return super.convertToWithSheetVo(comment);
    }

    @NonNull
    @Override
    public List<SheetCommentWithSheetVO> convertToWithSheetVo(
        List<SheetComment> sheetComments) {
        if (!CollectionUtils.isEmpty(sheetComments)) {
            sheetComments.forEach(this::clearSensitiveField);
        }
        return super.convertToWithSheetVo(sheetComments);
    }

    @NonNull
    @Override
    public Page<SheetCommentWithSheetVO> convertToWithSheetVo(
        @NonNull Page<SheetComment> sheetCommentPage) {
        sheetCommentPage.getContent().forEach(this::clearSensitiveField);
        return super.convertToWithSheetVo(sheetCommentPage);
    }

    @NonNull
    @Override
    public Page<BaseCommentVO> pageVosBy(@NonNull List<SheetComment> sheetComments,
        @NonNull Pageable pageable) {
        sheetComments.forEach(this::clearSensitiveField);
        return super.pageVosBy(sheetComments, pageable);
    }
}
