package run.halo.app.controller.admin.api;

import io.swagger.annotations.ApiOperation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import run.halo.app.model.entity.Sheet;
import run.halo.app.model.enums.PostStatus;
import run.halo.app.model.params.SheetParam;
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
    public Sheet getBy(@PathVariable("sheetId") Integer sheetId) {
        return sheetService.getById(sheetId);
    }

    @GetMapping
    @ApiOperation("Gets a page of sheet")
    public Page<Sheet> pageBy(@PageableDefault(sort = "editTime", direction = DESC) Pageable pageable) {
        return sheetService.pageBy(pageable);
    }

    @PostMapping
    @ApiOperation("Creates a sheet")
    public Sheet createBy(@RequestBody @Valid SheetParam sheetParam) {
        return sheetService.createBy(sheetParam.convertTo());
    }

    @PutMapping("{sheetId:\\d+}")
    @ApiOperation("Updates a sheet")
    public Sheet updateBy(
            @PathVariable("sheetId") Integer sheetId,
            @RequestBody @Valid SheetParam sheetParam) {
        return sheetService.updateBy(sheetParam.convertTo());
    }

    @PutMapping("{sheetId:\\d+}/{status}")
    public Sheet updateStatusBy(
            @PathVariable("sheetId") Integer sheetId,
            @PathVariable("status") PostStatus status) {
        Sheet sheet = sheetService.getById(sheetId);

        // Set status
        sheet.setStatus(status);

        // Update and return
        return sheetService.update(sheet);
    }

    @DeleteMapping("{sheetId:\\d+}")
    @ApiOperation("Deletes a sheet")
    public Sheet deleteBy(@PathVariable("sheetId") Integer sheetId) {
        return sheetService.removeById(sheetId);
    }
}
