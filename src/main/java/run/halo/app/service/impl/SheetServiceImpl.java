package run.halo.app.service.impl;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import run.halo.app.event.post.SheetVisitEvent;
import run.halo.app.model.entity.Sheet;
import run.halo.app.model.enums.PostStatus;
import run.halo.app.model.vo.SheetListVO;
import run.halo.app.repository.SheetRepository;
import run.halo.app.service.OptionService;
import run.halo.app.service.SheetCommentService;
import run.halo.app.service.SheetService;
import run.halo.app.utils.ServiceUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Sheet service implementation.
 *
 * @author johnniang
 * @date 19-4-24
 */
@Service
public class SheetServiceImpl extends BasePostServiceImpl<Sheet> implements SheetService {

    private final SheetRepository sheetRepository;

    private final ApplicationEventPublisher eventPublisher;

    private final SheetCommentService sheetCommentService;

    public SheetServiceImpl(SheetRepository sheetRepository,
                            ApplicationEventPublisher eventPublisher,
                            SheetCommentService sheetCommentService,
                            OptionService optionService) {
        super(sheetRepository, optionService);
        this.sheetRepository = sheetRepository;
        this.eventPublisher = eventPublisher;
        this.sheetCommentService = sheetCommentService;
    }

    @Override
    public Sheet createBy(Sheet sheet) {
        return createOrUpdateBy(sheet);
    }

    @Override
    public Sheet updateBy(Sheet sheet) {
        return createOrUpdateBy(sheet);
    }

    @Override
    public Page<Sheet> pageBy(Pageable pageable) {
        Assert.notNull(pageable, "Page info must not be null");

        return listAll(pageable);
    }

    /**
     * Gets sheet by post status and url.
     *
     * @param status post status must not be null
     * @param url    sheet url must not be blank
     * @return sheet info
     */
    @Override
    public Sheet getBy(PostStatus status, String url) {
        Sheet sheet = super.getBy(status, url);

        if (PostStatus.PUBLISHED.equals(status)) {
            // Log it
            eventPublisher.publishEvent(new SheetVisitEvent(this, sheet.getId()));
        }

        return sheet;
    }

    @Override
    public Page<SheetListVO> convertToListVo(Page<Sheet> sheetPage) {
        Assert.notNull(sheetPage, "Sheet page must not be null");

        // Get all sheet id
        List<Sheet> sheets = sheetPage.getContent();

        Set<Integer> sheetIds = ServiceUtils.fetchProperty(sheets, Sheet::getId);

        // key: sheet id, value: comment count
        Map<Integer, Long> sheetCommentCountMap = sheetCommentService.countByPostIds(sheetIds);

        return sheetPage.map(sheet -> {
            SheetListVO sheetListVO = new SheetListVO().convertFrom(sheet);
            sheetListVO.setCommentCount(sheetCommentCountMap.getOrDefault(sheet.getId(), 0L));
            return sheetListVO;
        });
    }

}
