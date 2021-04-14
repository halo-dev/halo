package run.halo.app.controller.content.api;

import static java.util.stream.Collectors.toMap;

import io.swagger.annotations.ApiOperation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import run.halo.app.model.dto.OptionDTO;
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
        var options = optionService.listDtos();
        var optionMap = options.stream()
            .collect(toMap(OptionDTO::getKey, option -> option));
        var keys = options.stream()
            .map(OptionDTO::getKey)
            .collect(Collectors.toUnmodifiableSet());
        return optionFilter.filter(keys).stream()
            .map(optionMap::get)
            .collect(Collectors.toUnmodifiableList());
    }

    @GetMapping("map_view")
    @ApiOperation("Lists options with map view")
    public Map<String, Object> listAllWithMapView(
        @Deprecated(since = "1.4.8", forRemoval = true)
        @RequestParam(value = "key", required = false) List<String> keyList,
        @RequestParam(value = "keys", required = false) String keys) {
        // handle for key list
        if (!CollectionUtils.isEmpty(keyList)) {
            return optionService.listOptions(optionFilter.filter(keyList));
        }
        // handle for keys
        if (StringUtils.hasText(keys)) {
            var nameSet = Arrays.stream(keys.split(","))
                .map(String::trim)
                .collect(Collectors.toUnmodifiableSet());
            var filteredNames = optionFilter.filter(nameSet);
            return optionService.listOptions(filteredNames);
        }
        // list all
        Map<String, Object> options = optionService.listOptions();
        return optionFilter.filter(options.keySet()).stream()
            .collect(toMap(optionName -> optionName, options::get));
    }

    @GetMapping("keys/{key}")
    @ApiOperation("Gets option value by option key")
    public BaseResponse<Object> getBy(@PathVariable("key") String key) {
        Object optionValue = optionFilter.filter(key)
            .flatMap(k -> optionService.getByKey(key))
            .orElse(null);
        return BaseResponse.ok(optionValue);
    }

    @GetMapping("comment")
    @ApiOperation("Options for comment(@deprecated, use /bulk api instead of this.)")
    @Deprecated
    public Map<String, Object> comment() {
        List<String> keys = new ArrayList<>();
        keys.add(CommentProperties.GRAVATAR_DEFAULT.getValue());
        keys.add(CommentProperties.CONTENT_PLACEHOLDER.getValue());
        keys.add(CommentProperties.GRAVATAR_SOURCE.getValue());
        return optionService.listOptions(optionFilter.filter(keys));
    }

}
