package run.halo.app.service.impl;

import cn.hutool.core.text.StrBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import run.halo.app.event.logger.LogEvent;
import run.halo.app.event.post.SheetVisitEvent;
import run.halo.app.exception.AlreadyExistsException;
import run.halo.app.exception.NotFoundException;
import run.halo.app.model.dto.InternalSheetDTO;
import run.halo.app.model.entity.Sheet;
import run.halo.app.model.entity.SheetComment;
import run.halo.app.model.entity.SheetMeta;
import run.halo.app.model.enums.LogType;
import run.halo.app.model.enums.PostStatus;
import run.halo.app.model.vo.SheetDetailVO;
import run.halo.app.model.vo.SheetListVO;
import run.halo.app.repository.SheetRepository;
import run.halo.app.service.*;
import run.halo.app.utils.MarkdownUtils;
import run.halo.app.utils.ServiceUtils;

import java.util.*;

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

    public SheetServiceImpl(SheetRepository sheetRepository,
                            ApplicationEventPublisher eventPublisher,
                            SheetCommentService sheetCommentService,
                            OptionService optionService,
                            SheetMetaService sheetMetaService,
                            ThemeService themeService) {
        super(sheetRepository, optionService);
        this.sheetRepository = sheetRepository;
        this.eventPublisher = eventPublisher;
        this.sheetCommentService = sheetCommentService;
        this.sheetMetaService = sheetMetaService;
        this.themeService = themeService;
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
    public Sheet createBy(Sheet sheet, Set<SheetMeta> sheetMetas, boolean autoSave) {
        Sheet createdSheet = createOrUpdateBy(sheet);

        // Create sheet meta data
        List<SheetMeta> sheetMetaList = sheetMetaService.createOrUpdateByPostId(sheet.getId(), sheetMetas);
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
    public Sheet updateBy(Sheet sheet, Set<SheetMeta> sheetMetas, boolean autoSave) {
        Sheet updatedSheet = createOrUpdateBy(sheet);

        // Create sheet meta data
        List<SheetMeta> sheetMetaList = sheetMetaService.createOrUpdateByPostId(updatedSheet.getId(), sheetMetas);
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
    public Sheet getByUrl(String url) {
        Assert.hasText(url, "Url must not be blank");

        Sheet sheet = sheetRepository.getByUrl(url).orElseThrow(() -> new NotFoundException("查询不到该页面的信息").setErrorData(url));

        return sheet;
    }

    @Override
    public Sheet getBy(PostStatus status, String url) {
        Assert.notNull(status, "Post status must not be null");
        Assert.hasText(url, "Post url must not be blank");

        Optional<Sheet> postOptional = sheetRepository.getByUrlAndStatus(url, status);

        Sheet sheet = postOptional.orElseThrow(() -> new NotFoundException("查询不到该页面的信息").setErrorData(url));

        return sheet;
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

        StrBuilder content = new StrBuilder("---\n");

        content.append("type: ").append("sheet").append("\n");
        content.append("title: ").append(sheet.getTitle()).append("\n");
        content.append("permalink: ").append(sheet.getUrl()).append("\n");
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
    public List<InternalSheetDTO> listInternal() {

        List<InternalSheetDTO> internalSheetDTOS = new ArrayList<>();

        // links sheet
        InternalSheetDTO linkSheet = new InternalSheetDTO();
        linkSheet.setId(1);
        linkSheet.setTitle("友情链接");
        linkSheet.setUrl("/links");
        linkSheet.setStatus(themeService.templateExists("links.ftl"));

        // photos sheet
        InternalSheetDTO photoSheet = new InternalSheetDTO();
        photoSheet.setId(2);
        photoSheet.setTitle("图库页面");
        photoSheet.setUrl("/photos");
        photoSheet.setStatus(themeService.templateExists("photos.ftl"));

        // journals sheet
        InternalSheetDTO journalSheet = new InternalSheetDTO();
        journalSheet.setId(3);
        journalSheet.setTitle("日志页面");
        journalSheet.setUrl("/journals");
        journalSheet.setStatus(themeService.templateExists("journals.ftl"));

        internalSheetDTOS.add(linkSheet);
        internalSheetDTOS.add(photoSheet);
        internalSheetDTOS.add(journalSheet);

        return internalSheetDTOS;
    }

    @Override
    public Sheet removeById(Integer id) {

        // Remove sheet metas
        List<SheetMeta> sheetMetas = sheetMetaService.removeByPostId(id);
        log.debug("Removed sheet metas: [{}]", sheetMetas);

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
            return sheetListVO;
        });
    }

    @Override
    public void publishVisitEvent(Integer sheetId) {
        eventPublisher.publishEvent(new SheetVisitEvent(this, sheetId));
    }

    @Override
    public SheetDetailVO convertToDetailVo(Sheet sheet) {
        // List sheetMetas
        List<SheetMeta> sheetMetas = sheetMetaService.listBy(sheet.getId());
        // Convert to detail vo
        return convertTo(sheet, sheetMetas);
    }

    @NonNull
    private SheetDetailVO convertTo(@NonNull Sheet sheet, List<SheetMeta> sheetMetas) {
        Assert.notNull(sheet, "Sheet must not be null");

        // Convert to base detail vo
        SheetDetailVO sheetDetailVO = new SheetDetailVO().convertFrom(sheet);

        Set<Long> sheetMetaIds = ServiceUtils.fetchProperty(sheetMetas, SheetMeta::getId);

        // Get sheet meta ids
        sheetDetailVO.setSheetMetaIds(sheetMetaIds);
        sheetDetailVO.setSheetMetas(sheetMetaService.convertTo(sheetMetas));

        if (StringUtils.isBlank(sheetDetailVO.getSummary())) {
            sheetDetailVO.setSummary(generateSummary(sheet.getFormatContent()));
        }

        sheetDetailVO.setCommentCount(sheetCommentService.countByPostId(sheet.getId()));
        return sheetDetailVO;
    }

    @Override
    protected void urlMustNotExist(Sheet sheet) {
        Assert.notNull(sheet, "Sheet must not be null");

        // Get url count
        boolean exist;

        if (ServiceUtils.isEmptyId(sheet.getId())) {
            // The sheet will be created
            exist = sheetRepository.existsByUrl(sheet.getUrl());
        } else {
            // The sheet will be updated
            exist = sheetRepository.existsByIdNotAndUrl(sheet.getId(), sheet.getUrl());
        }

        if (exist) {
            throw new AlreadyExistsException("页面路径 " + sheet.getUrl() + " 已存在");
        }
    }
}
