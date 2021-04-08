package run.halo.app.controller.content.api;

import io.swagger.annotations.ApiOperation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import run.halo.app.model.dto.OptionDTO;
import run.halo.app.model.properties.AliOssProperties;
import run.halo.app.model.properties.ApiProperties;
import run.halo.app.model.properties.CommentProperties;
import run.halo.app.model.support.BaseResponse;
import run.halo.app.service.OptionService;
import run.halo.app.service.impl.OptionFilter;

/**
 * Content option controller.
 *
 * @author johnniang
 * @date 2019-04-03
 */
@RestController("ApiContentOptionController")
@RequestMapping("/api/content/options")
public class OptionController {

    private final OptionService optionService;

    private final OptionFilter optionFilter;

    public OptionController(OptionService optionService) {
        this.optionService = optionService;
        optionFilter = new OptionFilter(optionService);
    }

    @GetMapping("list_view")
    @ApiOperation("Lists all options with list view")
    public List<OptionDTO> listAll() {
        return optionService.listDtos();
    }

    @GetMapping("map_view")
    @ApiOperation("Lists options with map view")
    public Map<String, Object> listAllWithMapView(
        @RequestParam(value = "key", required = false) List<String> keys) {
        if (CollectionUtils.isEmpty(keys)) {
            return optionService.listOptions();
        }

        return optionService.listOptions(keys);
    }

    @GetMapping("keys/{key}")
    @ApiOperation("Gets option value by option key")
    public BaseResponse<Object> getBy(@PathVariable("key") String key) {
        return BaseResponse
            .ok(HttpStatus.OK.getReasonPhrase(), optionService.getByKey(key).orElse(null));
    }

    @GetMapping("comment")
    @ApiOperation("Options for comment")
    public Map<String, Object> comment() {
        List<String> keys = new ArrayList<>();
        keys.add(CommentProperties.GRAVATAR_DEFAULT.getValue());
        keys.add(CommentProperties.CONTENT_PLACEHOLDER.getValue());
        keys.add(CommentProperties.GRAVATAR_SOURCE.getValue());
        return optionService.listOptions(keys);
    }

    @GetMapping("/bulk")
    @ApiOperation("Lists options by providing option names")
    public Map<String, Object> listByNames(@RequestParam("names") String names) {
        var nameSet = Arrays.stream(names.split(","))
            .map(String::trim)
            .collect(Collectors.toUnmodifiableSet());
        var filteredNames = optionFilter.filter(nameSet);
        return optionService.listOptions(filteredNames);
    }
}
