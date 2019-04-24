package run.halo.app.service.impl;

import run.halo.app.model.entity.SheetComment;
import run.halo.app.repository.SheetCommentRepository;
import run.halo.app.service.SheetCommentService;
import run.halo.app.service.base.AbstractCrudService;

/**
 * Sheet comment service implementation.
 *
 * @author johnniang
 * @date 19-4-24
 */
public class SheetCommentServiceImpl extends AbstractCrudService<SheetComment, Long> implements SheetCommentService {

    private final SheetCommentRepository sheetCommentRepository;

    public SheetCommentServiceImpl(SheetCommentRepository sheetCommentRepository) {
        super(sheetCommentRepository);
        this.sheetCommentRepository = sheetCommentRepository;
    }
}
