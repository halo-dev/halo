package run.halo.app.service.impl;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import run.halo.app.exception.BadRequestException;
import run.halo.app.exception.NotFoundException;
import run.halo.app.model.entity.Sheet;
import run.halo.app.model.entity.SheetComment;
import run.halo.app.repository.SheetCommentRepository;
import run.halo.app.repository.SheetRepository;
import run.halo.app.service.OptionService;
import run.halo.app.service.SheetCommentService;
import run.halo.app.service.UserService;
import run.halo.app.service.assembler.comment.SheetCommentAssembler;

/**
 * Sheet comment service implementation.
 *
 * @author johnniang
 * @author ryanwang
 * @date 2019-04-24
 */
@Service
public class SheetCommentServiceImpl extends BaseCommentServiceImpl<SheetComment>
    implements SheetCommentService {

    private final SheetRepository sheetRepository;

    public SheetCommentServiceImpl(SheetCommentRepository sheetCommentRepository,
        OptionService optionService,
        UserService userService,
        ApplicationEventPublisher eventPublisher,
        SheetRepository sheetRepository,
        SheetCommentAssembler sheetCommentAssembler) {
        super(sheetCommentRepository, optionService, userService, eventPublisher,
            sheetCommentAssembler);
        this.sheetRepository = sheetRepository;
    }

    @Override
    public void validateTarget(@NonNull Integer sheetId) {
        Sheet sheet = sheetRepository.findById(sheetId)
            .orElseThrow(() -> new NotFoundException("查询不到该页面的信息").setErrorData(sheetId));

        if (sheet.getDisallowComment()) {
            throw new BadRequestException("该页面已被禁止评论").setErrorData(sheetId);
        }
    }
}
