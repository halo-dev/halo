package run.halo.app.service.impl;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import run.halo.app.event.post.VisitEvent;
import run.halo.app.exception.AlreadyExistsException;
import run.halo.app.exception.NotFoundException;
import run.halo.app.model.dto.post.SheetDetailDTO;
import run.halo.app.model.entity.Post;
import run.halo.app.model.entity.Sheet;
import run.halo.app.model.enums.PostStatus;
import run.halo.app.repository.SheetRepository;
import run.halo.app.service.SheetService;
import run.halo.app.service.base.AbstractCrudService;
import run.halo.app.utils.DateUtils;
import run.halo.app.utils.MarkdownUtils;
import run.halo.app.utils.ServiceUtils;

import java.util.Optional;

/**
 * Sheet service implementation.
 *
 * @author johnniang
 * @date 19-4-24
 */
@Service
public class SheetServiceImpl extends AbstractCrudService<Sheet, Integer> implements SheetService {

    private final SheetRepository sheetRepository;
    private final ApplicationEventPublisher eventPublisher;

    public SheetServiceImpl(SheetRepository sheetRepository,
                            ApplicationEventPublisher eventPublisher) {
        super(sheetRepository);
        this.sheetRepository = sheetRepository;
        this.eventPublisher = eventPublisher;
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
        Assert.notNull(status, "Post status must not be null");
        Assert.hasText(url, "Sheet url must not be blank");

        Optional<Sheet> sheetOptional = sheetRepository.getByUrlAndStatus(url, status);

        Sheet sheet = sheetOptional.orElseThrow(() -> new NotFoundException("The sheet with status " + status + " and url " + url + "was not existed").setErrorData(url));

        if (PostStatus.PUBLISHED.equals(status)) {
            // Log it
            eventPublisher.publishEvent(new VisitEvent(this, sheet.getId()));
        }

        return sheet;
    }

    @Override
    public SheetDetailDTO convertToDetailDto(Sheet sheet) {
        Assert.notNull(sheet, "Sheet must not be null");

        // Convert and return
        return new SheetDetailDTO().convertFrom(sheet);
    }

    @NonNull
    private Sheet createOrUpdateBy(@NonNull Sheet sheet) {
        Assert.notNull(sheet, "Sheet must not be null");

        // Check url
        urlMustNotExist(sheet);

        // Render content
        sheet.setFormatContent(MarkdownUtils.renderMarkdown(sheet.getOriginalContent()));

        // Create or update post
        if (ServiceUtils.isEmptyId(sheet.getId())) {
            // The sheet will be created
            return create(sheet);
        }

        // The sheet will be updated
        // Set edit time
        sheet.setEditTime(DateUtils.now());
        // Update it
        return update(sheet);
    }

    /**
     * Check if the url is exist.
     *
     * @param sheet sheet must not be null
     */
    private void urlMustNotExist(@NonNull Sheet sheet) {
        Assert.notNull(sheet, "Sheet must not be null");
        // TODO Refactor this method with BasePostService

        // TODO May refactor these queries
        // Get url count
        long count;
        if (ServiceUtils.isEmptyId(sheet.getId())) {
            // The sheet will be created
            count = sheetRepository.countByUrl(sheet.getUrl());
        } else {
            // The sheet will be updated
            count = sheetRepository.countByIdNotAndUrl(sheet.getId(), sheet.getUrl());
        }

        if (count > 0) {
            throw new AlreadyExistsException("The sheet url has been exist");
        }
    }
}
