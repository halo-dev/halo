package run.halo.app.web.controller.content.api;

import run.halo.app.model.dto.OptionDTO;
import run.halo.app.model.support.BaseResponse;
import run.halo.app.service.OptionService;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Portal option controller.
 *
 * @author johnniang
 * @date 4/3/19
 */
@RestController("PortalOptionController")
@RequestMapping("/api/options")
public class OptionController {

    private final OptionService optionService;

    public OptionController(OptionService optionService) {
        this.optionService = optionService;
    }

    @GetMapping("list_view")
    @ApiOperation("Lists all options with list view")
    public List<OptionDTO> listAll() {
        return optionService.listDtos();
    }

    @GetMapping("map_view")
    @ApiOperation("Lists all options with map view")
    public Map<String, String> listAllWithMapView() {
        return optionService.listOptions();
    }

    @GetMapping("keys/{key}")
    @ApiOperation("Gets option value by option key")
    public BaseResponse<String> getBy(@PathVariable("key") String key) {
        return BaseResponse.ok(HttpStatus.OK.getReasonPhrase(), optionService.getByKey(key).orElse(""));
    }
}
