package run.halo.app.service.impl;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import run.halo.app.model.entity.SheetComment;
import run.halo.app.repository.PostRepository;
import run.halo.app.repository.SheetCommentRepository;
import run.halo.app.service.OptionService;
import run.halo.app.service.SheetCommentService;

/**
 * Sheet comment service implementation.
 *
 * @author johnniang
 * @date 19-4-24
 */
@Service
public class SheetCommentServiceImpl extends BaseCommentServiceImpl<SheetComment> implements SheetCommentService {

    private final SheetCommentRepository sheetCommentRepository;

    public SheetCommentServiceImpl(SheetCommentRepository sheetCommentRepository,
                                   PostRepository postRepository,
                                   OptionService optionService,
                                   ApplicationEventPublisher eventPublisher) {
        super(sheetCommentRepository, postRepository, optionService, eventPublisher);
        this.sheetCommentRepository = sheetCommentRepository;
    }
}
