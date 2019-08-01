package run.halo.app.controller.content.api;

import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import run.halo.app.model.dto.OptionDTO;
import run.halo.app.model.support.BaseResponse;
import run.halo.app.service.OptionService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Portal option controller.
 *
 * @author johnniang
 * @date 4/3/19
 */
@RestController("ApiContentOptionController")
@RequestMapping("/api/content/options")
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
    @ApiOperation("Lists options with map view")
    public Map<String, Object> listAllWithMapView(@RequestParam(value = "key", required = false) List<String> keys) {
        if (CollectionUtils.isEmpty(keys)) {
            return optionService.listOptions();
        }

        return optionService.listOptions(keys);
    }

    @GetMapping("keys/{key}")
    @ApiOperation("Gets option value by option key")
    public BaseResponse<Object> getBy(@PathVariable("key") String key) {
        return BaseResponse.ok(HttpStatus.OK.getReasonPhrase(), optionService.getByKey(key).orElse(null));
    }


    @GetMapping("comment")
    @ApiOperation("Options for comment")
    public Map<String, Object> comment() {
        List<String> keys = new ArrayList<>();
        keys.add("comment_gravatar_default");
        keys.add("comment_content_placeholder");
        return optionService.listOptions(keys);
    }
}
