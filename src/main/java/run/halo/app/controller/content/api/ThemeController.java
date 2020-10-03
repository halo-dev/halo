package run.halo.app.controller.content.api;

import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import run.halo.app.handler.theme.config.support.ThemeProperty;
import run.halo.app.service.ThemeService;
import run.halo.app.service.ThemeSettingService;

import java.util.Map;

/**
 * Content theme controller.
 *
 * @author ryanwang
 * @date 2020-01-17
 */
@RestController("ApiContentThemeController")
@RequestMapping("/api/content/themes")
public class ThemeController {

    private final ThemeService themeService;

    private final ThemeSettingService themeSettingService;

    public ThemeController(ThemeService themeService, ThemeSettingService themeSettingService) {
        this.themeService = themeService;
        this.themeSettingService = themeSettingService;
    }

    @GetMapping("activation")
    @ApiOperation("Gets activated theme property")
    public ThemeProperty getBy() {
        return themeService.getThemeOfNonNullBy(themeService.getActivatedThemeId());
    }

    @GetMapping("activation/settings")
    @ApiOperation("Lists activated theme settings")
    public Map<String, Object> listSettingsBy() {
        return themeSettingService.listAsMapBy(themeService.getActivatedThemeId());
    }
}
