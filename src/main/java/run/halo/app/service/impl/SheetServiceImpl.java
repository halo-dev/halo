package run.halo.app.service.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import run.halo.app.event.logger.LogEvent;
import run.halo.app.event.post.SheetVisitEvent;
import run.halo.app.exception.AlreadyExistsException;
import run.halo.app.exception.NotFoundException;
import run.halo.app.model.dto.IndependentSheetDTO;
import run.halo.app.model.entity.Content;
import run.halo.app.model.entity.Content.PatchedContent;
import run.halo.app.model.entity.Sheet;
import run.halo.app.model.entity.SheetComment;
import run.halo.app.model.entity.SheetMeta;
import run.halo.app.model.enums.LogType;
import run.halo.app.model.enums.PostStatus;
import run.halo.app.repository.SheetRepository;
import run.halo.app.service.ContentPatchLogService;
import run.halo.app.service.ContentService;
import run.halo.app.service.OptionService;
import run.halo.app.service.SheetCommentService;
import run.halo.app.service.SheetMetaService;
import run.halo.app.service.SheetService;
import run.halo.app.service.ThemeService;
import run.halo.app.utils.MarkdownUtils;
import run.halo.app.utils.ServiceUtils;

/**
 * Sheet service implementation.
 *
 * @author johnniang
 * @author ryanwang
 * @author evanwang
 * @date 2019-04-24
 */
@Slf4j
@Service
public class SheetServiceImpl extends BasePostServiceImpl<Sheet>
    implements SheetService {

    private final SheetRepository sheetRepository;

    private final ApplicationEventPublisher eventPublisher;

    private final SheetCommentService sheetCommentService;

    private final SheetMetaService sheetMetaService;

    private final ThemeService themeService;

    private final OptionService optionService;

    private final ContentService sheetContentService;

    private final ContentPatchLogService sheetContentPatchLogService;

    public SheetServiceImpl(SheetRepository sheetRepository,
        ApplicationEventPublisher eventPublisher,
        SheetCommentService sheetCommentService,
        ContentService sheetContentService,
        SheetMetaService sheetMetaService,
        ThemeService themeService,
        OptionService optionService,
        ContentPatchLogService sheetContentPatchLogService) {
        super(sheetRepository, optionService, sheetContentService, sheetContentPatchLogService);
        this.sheetRepository = sheetRepository;
        this.eventPublisher = eventPublisher;
        this.sheetCommentService = sheetCommentService;
        this.sheetMetaService = sheetMetaService;
        this.themeService = themeService;
        this.optionService = optionService;
        this.sheetContentService = sheetContentService;
        this.sheetContentPatchLogService = sheetContentPatchLogService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Sheet createBy(Sheet sheet, boolean autoSave) {
        Sheet createdSheet = createOrUpdateBy(sheet);
        if (!autoSave) {
            // Log the creation
            LogEvent logEvent =
                new LogEvent(this, createdSheet.getId().toString(), LogType.SHEET_PUBLISHED,
                    createdSheet.getTitle());
            eventPublisher.publishEvent(logEvent);
        }
        return createdSheet;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Sheet createBy(Sheet sheet, Set<SheetMeta> metas, boolean autoSave) {
        Sheet createdSheet = createOrUpdateBy(sheet);

        // Create sheet meta data
        List<SheetMeta> sheetMetaList =
            sheetMetaService.createOrUpdateByPostId(sheet.getId(), metas);
        log.debug("Created sheet metas: [{}]", sheetMetaList);

        if (!autoSave) {
            // Log the creation
            LogEvent logEvent =
                new LogEvent(this, createdSheet.getId().toString(), LogType.SHEET_PUBLISHED,
                    createdSheet.getTitle());
            eventPublisher.publishEvent(logEvent);
        }
        return createdSheet;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Sheet updateBy(Sheet sheet, boolean autoSave) {
        Sheet updatedSheet = createOrUpdateBy(sheet);
        if (!autoSave) {
            // Log the creation
            LogEvent logEvent =
                new LogEvent(this, updatedSheet.getId().toString(), LogType.SHEET_EDITED,
                    updatedSheet.getTitle());
            eventPublisher.publishEvent(logEvent);
        }
        return updatedSheet;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Sheet updateBy(Sheet sheet, Set<SheetMeta> metas, boolean autoSave) {
        Sheet updatedSheet = createOrUpdateBy(sheet);

        // Create sheet meta data
        List<SheetMeta> sheetMetaList =
            sheetMetaService.createOrUpdateByPostId(updatedSheet.getId(), metas);
        log.debug("Created sheet metas: [{}]", sheetMetaList);

        if (!autoSave) {
            // Log the creation
            LogEvent logEvent =
                new LogEvent(this, updatedSheet.getId().toString(), LogType.SHEET_EDITED,
                    updatedSheet.getTitle());
            eventPublisher.publishEvent(logEvent);
        }
        return updatedSheet;
    }

    @Override
    public Page<Sheet> pageBy(Pageable pageable) {
        Assert.notNull(pageable, "Page info must not be null");

        return listAll(pageable);
    }

    @Override
    public Sheet getBySlug(String slug) {
        Assert.hasText(slug, "Sheet slug must not be blank");

        return sheetRepository.getBySlug(slug)
            .orElseThrow(() -> new NotFoundException("查询不到该页面的信息").setErrorData(slug));
    }

    @Override
    public Sheet getWithLatestContentById(Integer postId) {
        Sheet sheet = getById(postId);
        Content sheetContent = getContentById(postId);
        // Use the head pointer stored in the post content.
        PatchedContent patchedContent =
            sheetContentPatchLogService.getPatchedContentById(sheetContent.getHeadPatchLogId());
        sheet.setContent(patchedContent);
        return sheet;
    }

    @Override
    public Sheet getBy(PostStatus status, String slug) {
        Assert.notNull(status, "Sheet status must not be null");
        Assert.hasText(slug, "Sheet slug must not be blank");

        Optional<Sheet> postOptional = sheetRepository.getBySlugAndStatus(slug, status);

        return postOptional
            .orElseThrow(() -> new NotFoundException("查询不到该页面的信息").setErrorData(slug));
    }

    @Override
    public Sheet importMarkdown(String markdown) {
        Assert.notNull(markdown, "Markdown document must not be null");

        // Render markdown to html document.
        String content = MarkdownUtils.renderHtml(markdown);

        // Gets frontMatter
        Map<String, List<String>> frontMatter = MarkdownUtils.getFrontMatter(markdown);

        // TODO
        return null;
    }

    @Override
    public String exportMarkdown(Integer id) {
        Assert.notNull(id, "sheet id must not be null");
        Sheet sheet = getById(id);
        return exportMarkdown(sheet);
    }

    @Override
    public String exportMarkdown(Sheet sheet) {
        Assert.notNull(sheet, "Sheet must not be null");

        StringBuilder content = new StringBuilder("---\n");

        content.append("type: ").append("sheet").append("\n");
        content.append("title: ").append(sheet.getTitle()).append("\n");
        content.append("permalink: ").append(sheet.getSlug()).append("\n");
        content.append("thumbnail: ").append(sheet.getThumbnail()).append("\n");
        content.append("status: ").append(sheet.getStatus()).append("\n");
        content.append("date: ").append(sheet.getCreateTime()).append("\n");
        content.append("updated: ").append(sheet.getEditTime()).append("\n");
        content.append("comments: ").append(!sheet.getDisallowComment()).append("\n");

        content.append("---\n\n");
        content.append(sheet.getContent().getOriginalContent());
        return content.toString();
    }

    @Override
    public List<IndependentSheetDTO> listIndependentSheets() {

        String context =
            (optionService.isEnabledAbsolutePath() ? optionService.getBlogBaseUrl() : "") + "/";

        // TODO 日后将重构该部分，提供接口用于拓展独立页面，以供插件系统使用。

        // links sheet
        IndependentSheetDTO linkSheet = new IndependentSheetDTO();
        linkSheet.setId(1);
        linkSheet.setTitle("友情链接");
        linkSheet.setFullPath(context + optionService.getLinksPrefix());
        linkSheet.setRouteName("LinkList");
        linkSheet.setAvailable(themeService.templateExists("links.ftl"));

        // photos sheet
        IndependentSheetDTO photoSheet = new IndependentSheetDTO();
        photoSheet.setId(2);
        photoSheet.setTitle("图库页面");
        photoSheet.setFullPath(context + optionService.getPhotosPrefix());
        photoSheet.setRouteName("PhotoList");
        photoSheet.setAvailable(themeService.templateExists("photos.ftl"));

        // journals sheet
        IndependentSheetDTO journalSheet = new IndependentSheetDTO();
        journalSheet.setId(3);
        journalSheet.setTitle("日志页面");
        journalSheet.setFullPath(context + optionService.getJournalsPrefix());
        journalSheet.setRouteName("JournalList");
        journalSheet.setAvailable(themeService.templateExists("journals.ftl"));

        return Arrays.asList(linkSheet, photoSheet, journalSheet);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Sheet removeById(Integer id) {

        // Remove sheet metas
        List<SheetMeta> metas = sheetMetaService.removeByPostId(id);
        log.debug("Removed sheet metas: [{}]", metas);

        // Remove sheet comments
        List<SheetComment> sheetComments = sheetCommentService.removeByPostId(id);
        log.debug("Removed sheet comments: [{}]", sheetComments);

        // Remove sheet content
        Content sheetContent = sheetContentService.removeById(id);
        log.debug("Removed sheet content: [{}]", sheetContent);

        Sheet sheet = super.removeById(id);
        sheet.setContent(PatchedContent.of(sheetContent));

        // Log it
        eventPublisher.publishEvent(
            new LogEvent(this, id.toString(), LogType.SHEET_DELETED, sheet.getTitle()));

        return sheet;
    }

    @Override
    public void publishVisitEvent(Integer sheetId) {
        eventPublisher.publishEvent(new SheetVisitEvent(this, sheetId));
    }

    @Override
    protected void slugMustNotExist(Sheet sheet) {
        Assert.notNull(sheet, "Sheet must not be null");

        // Get slug count
        boolean exist;

        if (ServiceUtils.isEmptyId(sheet.getId())) {
            // The sheet will be created
            exist = sheetRepository.existsBySlug(sheet.getSlug());
        } else {
            // The sheet will be updated
            exist = sheetRepository.existsByIdNotAndSlug(sheet.getId(), sheet.getSlug());
        }

        if (exist) {
            throw new AlreadyExistsException("页面别名 " + sheet.getSlug() + " 已存在");
        }
    }
}
