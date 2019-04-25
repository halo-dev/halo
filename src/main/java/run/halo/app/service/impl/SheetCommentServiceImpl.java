package run.halo.app.service.impl;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import run.halo.app.exception.NotFoundException;
import run.halo.app.model.entity.SheetComment;
import run.halo.app.repository.SheetCommentRepository;
import run.halo.app.repository.SheetRepository;
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

    private final SheetRepository sheetRepository;

    public SheetCommentServiceImpl(SheetCommentRepository sheetCommentRepository,
                                   OptionService optionService,
                                   ApplicationEventPublisher eventPublisher,
                                   SheetRepository sheetRepository) {
        super(sheetCommentRepository, optionService, eventPublisher);
        this.sheetCommentRepository = sheetCommentRepository;
        this.sheetRepository = sheetRepository;
    }

    @Override
    public void targetMustExist(Integer sheetId) {
        if (sheetRepository.existsById(sheetId)) {
            throw new NotFoundException("The sheet with id " + sheetId + " was not found");
        }
    }
}
