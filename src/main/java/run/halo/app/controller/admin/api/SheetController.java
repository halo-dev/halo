package run.halo.app.controller.admin.api;

import io.swagger.annotations.ApiOperation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import run.halo.app.model.dto.post.BasePostDetailDTO;
import run.halo.app.model.entity.Sheet;
import run.halo.app.model.enums.PostStatus;
import run.halo.app.model.params.SheetParam;
import run.halo.app.model.vo.SheetListVO;
import run.halo.app.service.SheetService;

import javax.validation.Valid;

import static org.springframework.data.domain.Sort.Direction.DESC;

/**
 * Sheet controller.
 *
 * @author johnniang
 * @date 19-4-24
 */
@RestController
@RequestMapping("/api/admin/sheets")
public class SheetController {

    private final SheetService sheetService;

    public SheetController(SheetService sheetService) {
        this.sheetService = sheetService;
    }

    @GetMapping("{sheetId:\\d+}")
    @ApiOperation("Gets a sheet")
    public BasePostDetailDTO getBy(@PathVariable("sheetId") Integer sheetId) {
        Sheet sheet = sheetService.getById(sheetId);
        return sheetService.convertToDetail(sheet);
    }

    @GetMapping
    @ApiOperation("Gets a page of sheet")
    public Page<SheetListVO> pageBy(@PageableDefault(sort = "editTime", direction = DESC) Pageable pageable) {
        Page<Sheet> sheetPage = sheetService.pageBy(pageable);
        return sheetService.convertToListVo(sheetPage);
    }

    @PostMapping
    @ApiOperation("Creates a sheet")
    public BasePostDetailDTO createBy(@RequestBody @Valid SheetParam sheetParam) {
        Sheet sheet = sheetService.createBy(sheetParam.convertTo());
        return sheetService.convertToDetail(sheet);
    }

    @PutMapping("{sheetId:\\d+}")
    @ApiOperation("Updates a sheet")
    public BasePostDetailDTO updateBy(
            @PathVariable("sheetId") Integer sheetId,
            @RequestBody @Valid SheetParam sheetParam) {
        Sheet sheetToUpdate = sheetService.getById(sheetId);

        sheetParam.update(sheetToUpdate);

        Sheet sheet = sheetService.updateBy(sheetToUpdate);

        return sheetService.convertToDetail(sheet);
    }

    @PutMapping("{sheetId:\\d+}/{status}")
    public void updateStatusBy(
            @PathVariable("sheetId") Integer sheetId,
            @PathVariable("status") PostStatus status) {
        Sheet sheet = sheetService.getById(sheetId);

        // Set status
        sheet.setStatus(status);

        // Update
        sheetService.update(sheet);
    }

    @DeleteMapping("{sheetId:\\d+}")
    @ApiOperation("Deletes a sheet")
    public BasePostDetailDTO deleteBy(@PathVariable("sheetId") Integer sheetId) {
        Sheet sheet = sheetService.removeById(sheetId);
        return sheetService.convertToDetail(sheet);
    }
}
