package run.halo.app.controller.content.api;

import static org.springframework.data.domain.Sort.Direction.DESC;

import io.swagger.annotations.ApiOperation;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.HtmlUtils;
import run.halo.app.cache.lock.CacheLock;
import run.halo.app.model.dto.BaseCommentDTO;
import run.halo.app.model.entity.Sheet;
import run.halo.app.model.entity.SheetComment;
import run.halo.app.model.enums.CommentStatus;
import run.halo.app.model.enums.PostStatus;
import run.halo.app.model.params.SheetCommentParam;
import run.halo.app.model.vo.BaseCommentVO;
import run.halo.app.model.vo.BaseCommentWithParentVO;
import run.halo.app.model.vo.CommentWithHasChildrenVO;
import run.halo.app.model.vo.SheetDetailVO;
import run.halo.app.model.vo.SheetListVO;
import run.halo.app.service.OptionService;
import run.halo.app.service.SheetCommentService;
import run.halo.app.service.SheetService;
import run.halo.app.service.assembler.SheetRenderAssembler;
import run.halo.app.service.assembler.comment.SheetCommentRenderAssembler;

/**
 * Content sheet controller.
 *
 * @author johnniang
 * @author ryanwang
 * @date 2019-04-26
 */
@RestController("ApiContentSheetController")
@RequestMapping("/api/content/sheets")
public class SheetController {

    private final SheetCommentRenderAssembler sheetCommentRenderAssembler;

    private final SheetService sheetService;

    private final SheetRenderAssembler sheetRenderAssembler;

    private final SheetCommentService sheetCommentService;

    private final OptionService optionService;

    public SheetController(
        SheetCommentRenderAssembler sheetCommentRenderAssembler,
        SheetService sheetService,
        SheetRenderAssembler sheetRenderAssembler,
        SheetCommentService sheetCommentService,
        OptionService optionService) {
        this.sheetCommentRenderAssembler = sheetCommentRenderAssembler;
        this.sheetService = sheetService;
        this.sheetRenderAssembler = sheetRenderAssembler;
        this.sheetCommentService = sheetCommentService;
        this.optionService = optionService;
    }

    @GetMapping
    @ApiOperation("Lists sheets")
    public Page<SheetListVO> pageBy(
        @PageableDefault(sort = "createTime", direction = DESC) Pageable pageable) {
        Page<Sheet> sheetPage = sheetService.pageBy(PostStatus.PUBLISHED, pageable);
        return sheetRenderAssembler.convertToListVo(sheetPage);
    }

    @GetMapping("{sheetId:\\d+}")
    @ApiOperation("Gets a sheet")
    public SheetDetailVO getBy(@PathVariable("sheetId") Integer sheetId,
        @RequestParam(value = "formatDisabled", required = false, defaultValue = "true")
            Boolean formatDisabled,
        @RequestParam(value = "sourceDisabled", required = false, defaultValue = "false")
            Boolean sourceDisabled) {
        Sheet sheet = sheetService.getById(sheetId);

        SheetDetailVO sheetDetailVO = sheetRenderAssembler.convertToDetailVo(sheet);

        if (formatDisabled) {
            // Clear the format content
            sheetDetailVO.setContent(null);
        }

        if (sourceDisabled) {
            // Clear the original content
            sheetDetailVO.setOriginalContent(null);
        }

        sheetService.publishVisitEvent(sheetDetailVO.getId());

        return sheetDetailVO;
    }

    @GetMapping("/slug")
    @ApiOperation("Gets a sheet by slug")
    public SheetDetailVO getBy(@RequestParam("slug") String slug,
        @RequestParam(value = "formatDisabled", required = false, defaultValue = "true")
            Boolean formatDisabled,
        @RequestParam(value = "sourceDisabled", required = false, defaultValue = "false")
            Boolean sourceDisabled) {
        Sheet sheet = sheetService.getBySlug(slug);
        SheetDetailVO sheetDetailVO = sheetRenderAssembler.convertToDetailVo(sheet);

        if (formatDisabled) {
            // Clear the format content
            sheetDetailVO.setContent(null);
        }

        if (sourceDisabled) {
            // Clear the original content
            sheetDetailVO.setOriginalContent(null);
        }

        sheetService.publishVisitEvent(sheetDetailVO.getId());

        return sheetDetailVO;
    }

    @GetMapping("{sheetId:\\d+}/comments/top_view")
    public Page<CommentWithHasChildrenVO> listTopComments(@PathVariable("sheetId") Integer sheetId,
        @RequestParam(name = "page", required = false, defaultValue = "0") int page,
        @SortDefault(sort = "createTime", direction = DESC) Sort sort) {
        Page<CommentWithHasChildrenVO> comments =
            sheetCommentService.pageTopCommentsBy(sheetId, CommentStatus.PUBLISHED,
                PageRequest.of(page, optionService.getCommentPageSize(), sort));
        comments.forEach(sheetCommentRenderAssembler::clearSensitiveField);
        return comments;
    }

    @GetMapping("{sheetId:\\d+}/comments/{commentParentId:\\d+}/children")
    public List<BaseCommentDTO> listChildrenBy(@PathVariable("sheetId") Integer sheetId,
        @PathVariable("commentParentId") Long commentParentId,
        @SortDefault(sort = "createTime", direction = DESC) Sort sort) {
        // Find all children comments
        List<SheetComment> sheetComments = sheetCommentService
            .listChildrenBy(sheetId, commentParentId, CommentStatus.PUBLISHED, sort);
        // Convert to base comment dto
        return sheetCommentRenderAssembler.convertTo(sheetComments);
    }


    @GetMapping("{sheetId:\\d+}/comments/tree_view")
    @ApiOperation("Lists comments with tree view")
    public Page<BaseCommentVO> listCommentsTree(@PathVariable("sheetId") Integer sheetId,
        @RequestParam(name = "page", required = false, defaultValue = "0") int page,
        @SortDefault(sort = "createTime", direction = DESC) Sort sort) {
        Page<BaseCommentVO> comments = sheetCommentService
            .pageVosBy(sheetId, PageRequest.of(page, optionService.getCommentPageSize(), sort));
        comments.getContent().forEach(sheetCommentRenderAssembler::clearSensitiveField);
        return comments;
    }

    @GetMapping("{sheetId:\\d+}/comments/list_view")
    @ApiOperation("Lists comment with list view")
    public Page<BaseCommentWithParentVO> listComments(@PathVariable("sheetId") Integer sheetId,
        @RequestParam(name = "page", required = false, defaultValue = "0") int page,
        @SortDefault(sort = "createTime", direction = DESC) Sort sort) {
        Page<BaseCommentWithParentVO> comments =
            sheetCommentService.pageWithParentVoBy(sheetId,
                PageRequest.of(page, optionService.getCommentPageSize(), sort));
        comments.getContent().forEach(sheetCommentRenderAssembler::clearSensitiveField);
        return comments;
    }

    @PostMapping("comments")
    @ApiOperation("Comments a post")
    @CacheLock(autoDelete = false, traceRequest = true)
    public BaseCommentDTO comment(@RequestBody SheetCommentParam sheetCommentParam) {

        // Escape content
        sheetCommentParam.setContent(HtmlUtils
            .htmlEscape(sheetCommentParam.getContent(), StandardCharsets.UTF_8.displayName()));
        return sheetCommentRenderAssembler.convertTo(
            sheetCommentService.createBy(sheetCommentParam));
    }
}
