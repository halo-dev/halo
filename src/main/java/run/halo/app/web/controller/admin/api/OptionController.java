package run.halo.app.web.controller.admin.api;

import run.halo.app.model.dto.OptionOutputDTO;
import run.halo.app.model.enums.OptionSource;
import run.halo.app.model.params.OptionParam;
import run.halo.app.service.OptionService;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import run.halo.app.model.dto.OptionOutputDTO;
import run.halo.app.model.enums.OptionSource;
import run.halo.app.model.params.OptionParam;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * Option Controller.
 *
 * @author johnniang
 * @date 3/20/19
 */
@RestController
@RequestMapping("/admin/api/options")
public class OptionController {

    private final OptionService optionService;

    public OptionController(OptionService optionService) {
        this.optionService = optionService;
    }

    @GetMapping
    public List<OptionOutputDTO> listAll() {
        return optionService.listDtos();
    }

    @PostMapping("saving")
    public void saveOptions(@Valid @RequestBody List<OptionParam> optionParams) {
        optionService.save(optionParams, OptionSource.SYSTEM);
    }

    @GetMapping("map_view")
    @ApiOperation("Lists all options with map view")
    public Map<String, String> listAllWithMapView() {
        return optionService.listOptions();
    }

    @PostMapping("map_view/saving")
    @ApiOperation("Saves options by option map")
    public void saveOptionsWithMapView(@RequestBody Map<String, String> optionMap) {
        optionService.save(optionMap, OptionSource.SYSTEM);
    }
}
