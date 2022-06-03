package run.halo.app.service.assembler;

import static run.halo.app.model.support.HaloConst.URL_SEPARATOR;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import run.halo.app.model.dto.post.BasePostMinimalDTO;
import run.halo.app.model.entity.Content.PatchedContent;
import run.halo.app.model.entity.Sheet;
import run.halo.app.model.entity.SheetMeta;
import run.halo.app.model.enums.CommentStatus;
import run.halo.app.model.enums.SheetPermalinkType;
import run.halo.app.model.vo.SheetDetailVO;
import run.halo.app.model.vo.SheetListVO;
import run.halo.app.service.ContentService;
import run.halo.app.service.OptionService;
import run.halo.app.service.SheetCommentService;
import run.halo.app.service.SheetMetaService;
import run.halo.app.utils.ServiceUtils;

/**
 * Sheet assembler.
 *
 * @author guqing
 * @date 2022-03-01
 */
@Component
public class SheetAssembler extends BasePostAssembler<Sheet> {

    private final SheetCommentService sheetCommentService;

    private final ContentService contentService;

    private final SheetMetaService sheetMetaService;

    private final OptionService optionService;

    public SheetAssembler(SheetCommentService sheetCommentService,
        ContentService contentService,
        SheetMetaService sheetMetaService,
        OptionService optionService) {
        super(contentService, optionService);
        this.sheetCommentService = sheetCommentService;
        this.contentService = contentService;
        this.sheetMetaService = sheetMetaService;
        this.optionService = optionService;
    }

    /**
     * Converts to list dto page.
     *
     * @param sheetPage sheet page must not be nulls
     * @return a page of sheet list dto
     */
    @NonNull
    public Page<SheetListVO> convertToListVo(Page<Sheet> sheetPage) {
        Assert.notNull(sheetPage, "Sheet page must not be null");

        // Get all sheet id
        List<Sheet> sheets = sheetPage.getContent();

        Set<Integer> sheetIds = ServiceUtils.fetchProperty(sheets, Sheet::getId);

        // key: sheet id, value: comment count
        Map<Integer, Long> sheetCommentCountMap = sheetCommentService.countByStatusAndPostIds(
            CommentStatus.PUBLISHED, sheetIds);

        return sheetPage.map(sheet -> {
            SheetListVO sheetListVO = new SheetListVO().convertFrom(sheet);
            sheetListVO.setCommentCount(sheetCommentCountMap.getOrDefault(sheet.getId(), 0L));

            sheetListVO.setFullPath(buildFullPath(sheet));

            // Post currently drafting in process
            Boolean isInProcess = contentService.draftingInProgress(sheet.getId());
            sheetListVO.setInProgress(isInProcess);

            return sheetListVO;
        });
    }

    /**
     * Converts to detail vo.
     *
     * @param sheet sheet must not be null
     * @return sheet detail vo
     */
    @NonNull
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
    public SheetDetailVO convertTo(@NonNull Sheet sheet, List<SheetMeta> metas) {
        Assert.notNull(sheet, "Sheet must not be null");

        // Convert to base detail vo
        SheetDetailVO sheetDetailVO = new SheetDetailVO().convertFrom(sheet);

        Set<Long> metaIds = ServiceUtils.fetchProperty(metas, SheetMeta::getId);

        // Get sheet meta ids
        sheetDetailVO.setMetaIds(metaIds);
        sheetDetailVO.setMetas(sheetMetaService.convertTo(metas));

        generateAndSetSummaryIfAbsent(sheet, sheetDetailVO);

        sheetDetailVO.setCommentCount(sheetCommentService.countByStatusAndPostId(
            CommentStatus.PUBLISHED, sheet.getId()));

        sheetDetailVO.setFullPath(buildFullPath(sheet));

        PatchedContent sheetContent = sheet.getContent();
        sheetDetailVO.setContent(sheetContent.getContent());
        sheetDetailVO.setOriginalContent(sheetContent.getOriginalContent());

        // Sheet currently drafting in process
        Boolean inProgress = contentService.draftingInProgress(sheet.getId());
        sheetDetailVO.setInProgress(inProgress);

        return sheetDetailVO;
    }

    /**
     * Build sheet full path.
     *
     * @param sheet sheet
     * @return a full path to access.
     */
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
