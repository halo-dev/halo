package run.halo.app.service.impl;

import cn.hutool.core.text.StrBuilder;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import run.halo.app.event.logger.LogEvent;
import run.halo.app.event.post.SheetVisitEvent;
import run.halo.app.model.dto.InternalSheetDTO;
import run.halo.app.model.entity.Sheet;
import run.halo.app.model.enums.LogType;
import run.halo.app.model.enums.PostStatus;
import run.halo.app.model.vo.SheetListVO;
import run.halo.app.repository.SheetRepository;
import run.halo.app.service.OptionService;
import run.halo.app.service.SheetCommentService;
import run.halo.app.service.SheetService;
import run.halo.app.service.ThemeService;
import run.halo.app.utils.MarkdownUtils;
import run.halo.app.utils.ServiceUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Sheet service implementation.
 *
 * @author johnniang
 * @author ryanwang
 * @date 19-4-24
 */
@Service
public class SheetServiceImpl extends BasePostServiceImpl<Sheet> implements SheetService {

    private final SheetRepository sheetRepository;

    private final ApplicationEventPublisher eventPublisher;

    private final SheetCommentService sheetCommentService;

    private final ThemeService themeService;

    public SheetServiceImpl(SheetRepository sheetRepository,
                            ApplicationEventPublisher eventPublisher,
                            SheetCommentService sheetCommentService,
                            OptionService optionService,
                            ThemeService themeService) {
        super(sheetRepository, optionService);
        this.sheetRepository = sheetRepository;
        this.eventPublisher = eventPublisher;
        this.sheetCommentService = sheetCommentService;
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
    public Sheet importMarkdown(String markdown) {
        Assert.notNull(markdown, "Markdown document must not be null");

        // Render markdown to html document.
        String content = MarkdownUtils.renderMarkdown(markdown);

        // Gets frontMatter
        Map<String, List<String>> frontMatter = MarkdownUtils.getFrontMatter(markdown);

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

}
