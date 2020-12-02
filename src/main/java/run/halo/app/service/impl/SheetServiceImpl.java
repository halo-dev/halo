package run.halo.app.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import run.halo.app.event.logger.LogEvent;
import run.halo.app.event.post.SheetVisitEvent;
import run.halo.app.exception.AlreadyExistsException;
import run.halo.app.exception.NotFoundException;
import run.halo.app.model.dto.IndependentSheetDTO;
import run.halo.app.model.dto.post.BasePostMinimalDTO;
import run.halo.app.model.entity.Sheet;
import run.halo.app.model.entity.SheetComment;
import run.halo.app.model.entity.SheetMeta;
import run.halo.app.model.enums.LogType;
import run.halo.app.model.enums.PostStatus;
import run.halo.app.model.enums.SheetPermalinkType;
import run.halo.app.model.vo.SheetDetailVO;
import run.halo.app.model.vo.SheetListVO;
import run.halo.app.repository.SheetRepository;
import run.halo.app.service.*;
import run.halo.app.utils.MarkdownUtils;
import run.halo.app.utils.ServiceUtils;

import java.util.*;
import java.util.stream.Collectors;

import static run.halo.app.model.support.HaloConst.URL_SEPARATOR;

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
public class SheetServiceImpl extends BasePostServiceImpl<Sheet> implements SheetService {

    private final SheetRepository sheetRepository;

    private final ApplicationEventPublisher eventPublisher;

    private final SheetCommentService sheetCommentService;

    private final SheetMetaService sheetMetaService;

    private final ThemeService themeService;

    private final OptionService optionService;

    public SheetServiceImpl(SheetRepository sheetRepository,
            ApplicationEventPublisher eventPublisher,
            SheetCommentService sheetCommentService,
            SheetMetaService sheetMetaService,
            ThemeService themeService,
            OptionService optionService) {
        super(sheetRepository, optionService);
        this.sheetRepository = sheetRepository;
        this.eventPublisher = eventPublisher;
        this.sheetCommentService = sheetCommentService;
        this.sheetMetaService = sheetMetaService;
        this.themeService = themeService;
        this.optionService = optionService;
    }

    @Override
    public Sheet createBy(Sheet sheet, boolean autoSave) {
        Sheet createdSheet = createOrUpdateBy(sheet);
        if (!autoSave) {
            // Log the creation
            LogEvent logEvent = new LogEvent(this, createdSheet.getId().toString(), LogType.SHEET_PUBLISHED, createdSheet.getTitle());
            eventPublisher.publishEvent(logEvent);
        }
        return createdSheet;
    }

    @Override
    public Sheet createBy(Sheet sheet, Set<SheetMeta> metas, boolean autoSave) {
        Sheet createdSheet = createOrUpdateBy(sheet);

        // Create sheet meta data
        List<SheetMeta> sheetMetaList = sheetMetaService.createOrUpdateByPostId(sheet.getId(), metas);
        log.debug("Created sheet metas: [{}]", sheetMetaList);

        if (!autoSave) {
            // Log the creation
            LogEvent logEvent = new LogEvent(this, createdSheet.getId().toString(), LogType.SHEET_PUBLISHED, createdSheet.getTitle());
            eventPublisher.publishEvent(logEvent);
        }
        return createdSheet;
    }

    @Override
    public Sheet updateBy(Sheet sheet, boolean autoSave) {
        Sheet updatedSheet = createOrUpdateBy(sheet);
        if (!autoSave) {
            // Log the creation
            LogEvent logEvent = new LogEvent(this, updatedSheet.getId().toString(), LogType.SHEET_EDITED, updatedSheet.getTitle());
            eventPublisher.publishEvent(logEvent);
        }
        return updatedSheet;
    }

    @Override
    public Sheet updateBy(Sheet sheet, Set<SheetMeta> metas, boolean autoSave) {
        Sheet updatedSheet = createOrUpdateBy(sheet);

        // Create sheet meta data
        List<SheetMeta> sheetMetaList = sheetMetaService.createOrUpdateByPostId(updatedSheet.getId(), metas);
        log.debug("Created sheet metas: [{}]", sheetMetaList);

        if (!autoSave) {
            // Log the creation
            LogEvent logEvent = new LogEvent(this, updatedSheet.getId().toString(), LogType.SHEET_EDITED, updatedSheet.getTitle());
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

        return sheetRepository.getBySlug(slug).orElseThrow(() -> new NotFoundException("查询不到该页面的信息").setErrorData(slug));
    }

    @Override
    public Sheet getBy(PostStatus status, String slug) {
        Assert.notNull(status, "Sheet status must not be null");
        Assert.hasText(slug, "Sheet slug must not be blank");

        Optional<Sheet> postOptional = sheetRepository.getBySlugAndStatus(slug, status);

        return postOptional.orElseThrow(() -> new NotFoundException("查询不到该页面的信息").setErrorData(slug));
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
        content.append(sheet.getOriginalContent());
        return content.toString();
    }

    @Override
    public List<IndependentSheetDTO> listIndependentSheets() {

        String context = (optionService.isEnabledAbsolutePath() ? optionService.getBlogBaseUrl() : "") + "/";

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
    public Sheet removeById(Integer id) {

        // Remove sheet metas
        List<SheetMeta> metas = sheetMetaService.removeByPostId(id);
        log.debug("Removed sheet metas: [{}]", metas);

        // Remove sheet comments
        List<SheetComment> sheetComments = sheetCommentService.removeByPostId(id);
        log.debug("Removed sheet comments: [{}]", sheetComments);

        Sheet sheet = super.removeById(id);

        // Log it
        eventPublisher.publishEvent(new LogEvent(this, id.toString(), LogType.SHEET_DELETED, sheet.getTitle()));

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

            sheetListVO.setFullPath(buildFullPath(sheet));

            return sheetListVO;
        });
    }

    @Override
    public void publishVisitEvent(Integer sheetId) {
        eventPublisher.publishEvent(new SheetVisitEvent(this, sheetId));
    }

    @Override
    public SheetDetailVO convertToDetailVo(Sheet sheet) {
        // List metas
        List<SheetMeta> metas = sheetMetaService.listBy(sheet.getId());
        // Convert to detail vo
        return convertTo(sheet, metas);
    }

    @Override
    public BasePostMinimalDTO convertToMinimal(Sheet sheet) {
        Assert.notNull(sheet, "Sheet must not be null");
        BasePostMinimalDTO basePostMinimalDTO = new BasePostMinimalDTO().convertFrom(sheet);

        basePostMinimalDTO.setFullPath(buildFullPath(sheet));

        return basePostMinimalDTO;
    }

    @Override
    public List<BasePostMinimalDTO> convertToMinimal(List<Sheet> sheets) {
        if (CollectionUtils.isEmpty(sheets)) {
            return Collections.emptyList();
        }

        return sheets.stream()
                .map(this::convertToMinimal)
                .collect(Collectors.toList());
    }

    @NonNull
    private SheetDetailVO convertTo(@NonNull Sheet sheet, List<SheetMeta> metas) {
        Assert.notNull(sheet, "Sheet must not be null");

        // Convert to base detail vo
        SheetDetailVO sheetDetailVO = new SheetDetailVO().convertFrom(sheet);

        Set<Long> metaIds = ServiceUtils.fetchProperty(metas, SheetMeta::getId);

        // Get sheet meta ids
        sheetDetailVO.setMetaIds(metaIds);
        sheetDetailVO.setMetas(sheetMetaService.convertTo(metas));

        if (StringUtils.isBlank(sheetDetailVO.getSummary())) {
            sheetDetailVO.setSummary(generateSummary(sheet.getFormatContent()));
        }

        sheetDetailVO.setCommentCount(sheetCommentService.countByPostId(sheet.getId()));

        sheetDetailVO.setFullPath(buildFullPath(sheet));

        return sheetDetailVO;
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

    private String buildFullPath(Sheet sheet) {
        StringBuilder fullPath = new StringBuilder();

        SheetPermalinkType permalinkType = optionService.getSheetPermalinkType();

        if (optionService.isEnabledAbsolutePath()) {
            fullPath.append(optionService.getBlogBaseUrl());
        }

        if (permalinkType.equals(SheetPermalinkType.SECONDARY)) {
            fullPath.append(URL_SEPARATOR)
                    .append(optionService.getSheetPrefix())
                    .append(URL_SEPARATOR)
                    .append(sheet.getSlug())
                    .append(optionService.getPathSuffix());
        } else if (permalinkType.equals(SheetPermalinkType.ROOT)) {
            fullPath.append(URL_SEPARATOR)
                    .append(sheet.getSlug())
                    .append(optionService.getPathSuffix());
        }

        return fullPath.toString();
    }

}
