package run.halo.app.controller.content.api;

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
import run.halo.app.service.ClientOptionService;

/**
 * Content option controller.
 *
 * @author johnniang
 * @date 2019-04-03
 */
@RestController("ApiContentOptionController")
@RequestMapping("/api/content/options")
public class OptionController {

    private final ClientOptionService optionService;

    public OptionController(ClientOptionService clientOptionService) {
        this.optionService = clientOptionService;
    }

    @GetMapping("list_view")
    @ApiOperation("Lists all options with list view")
    public List<OptionDTO> listAll() {
        return optionService.listDtos();
    }

    @GetMapping("map_view")
    @ApiOperation("Lists options with map view")
    public Map<String, Object> listAllWithMapView(
        @Deprecated(since = "1.4.8", forRemoval = true)
        @RequestParam(value = "key", required = false) List<String> keyList,
        @RequestParam(value = "keys", required = false) String keys) {
        // handle for key list
        if (!CollectionUtils.isEmpty(keyList)) {
            return optionService.listOptions(keyList);
        }
        // handle for keys
        if (StringUtils.hasText(keys)) {
            var nameSet = Arrays.stream(keys.split(","))
                .map(String::trim)
                .collect(Collectors.toUnmodifiableSet());
            return optionService.listOptions(nameSet);
        }
        // list all
        return optionService.listOptions();
    }

    @GetMapping("keys/{key}")
    @ApiOperation("Gets option value by option key")
    public BaseResponse<Object> getBy(@PathVariable("key") String key) {
        Object optionValue = optionService.getByKey(key).orElse(null);
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
        return optionService.listOptions(keys);
    }

}
