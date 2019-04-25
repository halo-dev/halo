package run.halo.app.service;

import org.springframework.data.domain.Page;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import run.halo.app.model.entity.SheetComment;
import run.halo.app.model.vo.SheetCommentWithSheetVO;
import run.halo.app.service.base.BaseCommentService;

import java.util.List;

/**
 * Sheet comment service interface.
 *
 * @author johnniang
 * @date 19-4-24
 */
public interface SheetCommentService extends BaseCommentService<SheetComment> {

    @NonNull
    List<SheetCommentWithSheetVO> convertToWithPostVo(@Nullable List<SheetComment> sheetComments);

    @NonNull
    Page<SheetCommentWithSheetVO> convertToWithPostVo(@NonNull Page<SheetComment> sheetCommentPage);
}
