package run.halo.app.controller.content.api;

import io.swagger.annotations.ApiOperation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.web.bind.annotation.*;
import run.halo.app.cache.lock.CacheLock;
import run.halo.app.model.dto.BaseCommentDTO;
import run.halo.app.model.entity.SheetComment;
import run.halo.app.model.enums.CommentStatus;
import run.halo.app.model.params.SheetCommentParam;
import run.halo.app.model.vo.BaseCommentVO;
import run.halo.app.model.vo.BaseCommentWithParentVO;
import run.halo.app.model.vo.CommentWithHasChildrenVO;
import run.halo.app.service.OptionService;
import run.halo.app.service.SheetCommentService;
import run.halo.app.service.SheetService;

import java.util.List;

import static org.springframework.data.domain.Sort.Direction.DESC;

/**
 * Sheet controller.
 *
 * @author johnniang
 * @date 19-4-26
 */
@RestController("PortalSheetController")
@RequestMapping("/api/content/sheets")
public class SheetController {

    private final SheetService sheetService;

    private final SheetCommentService sheetCommentService;

    private final OptionService optionService;

    public SheetController(SheetService sheetService, SheetCommentService sheetCommentService, OptionService optionService) {
        this.sheetService = sheetService;
        this.sheetCommentService = sheetCommentService;
        this.optionService = optionService;
    }

    @GetMapping("{sheetId:\\d+}/comments/top_view")
    public Page<CommentWithHasChildrenVO> listTopComments(@PathVariable("sheetId") Integer sheetId,
                                                          @RequestParam(name = "page", required = false, defaultValue = "0") int page,
                                                          @SortDefault(sort = "createTime", direction = DESC) Sort sort) {
        Page<CommentWithHasChildrenVO> result = sheetCommentService.pageTopCommentsBy(sheetId, CommentStatus.PUBLISHED, PageRequest.of(page, optionService.getCommentPageSize(), sort));
        return sheetCommentService.filterIpAddress(result);
    }

    @GetMapping("{sheetId:\\d+}/comments/{commentParentId:\\d+}/children")
    public List<BaseCommentDTO> listChildrenBy(@PathVariable("sheetId") Integer sheetId,
                                               @PathVariable("commentParentId") Long commentParentId,
                                               @SortDefault(sort = "createTime", direction = DESC) Sort sort) {
        // Find all children comments
        List<SheetComment> sheetComments = sheetCommentService.listChildrenBy(sheetId, commentParentId, CommentStatus.PUBLISHED, sort);
        // Convert to base comment dto
        List<BaseCommentDTO> result = sheetCommentService.convertTo(sheetComments);
        return sheetCommentService.filterIpAddress(result);
    }


    @GetMapping("{sheetId:\\d+}/comments/tree_view")
    @ApiOperation("Lists comments with tree view")
    public Page<BaseCommentVO> listCommentsTree(@PathVariable("sheetId") Integer sheetId,
                                                @RequestParam(name = "page", required = false, defaultValue = "0") int page,
                                                @SortDefault(sort = "createTime", direction = DESC) Sort sort) {
        Page<BaseCommentVO> result = sheetCommentService.pageVosBy(sheetId, PageRequest.of(page, optionService.getCommentPageSize(), sort));
        return sheetCommentService.filterIpAddress(result);
    }

    @GetMapping("{sheetId:\\d+}/comments/list_view")
    @ApiOperation("Lists comment with list view")
    public Page<BaseCommentWithParentVO> listComments(@PathVariable("sheetId") Integer sheetId,
                                                      @RequestParam(name = "page", required = false, defaultValue = "0") int page,
                                                      @SortDefault(sort = "createTime", direction = DESC) Sort sort) {
        Page<BaseCommentWithParentVO> result = sheetCommentService.pageWithParentVoBy(sheetId, PageRequest.of(page, optionService.getCommentPageSize(), sort));
        return sheetCommentService.filterIpAddress(result);
    }

    @PostMapping("comments")
    @ApiOperation("Comments a post")
    @CacheLock(autoDelete = false, traceRequest = true)
    public BaseCommentDTO comment(@RequestBody SheetCommentParam sheetCommentParam) {
        return sheetCommentService.convertTo(sheetCommentService.createBy(sheetCommentParam));
    }
}
