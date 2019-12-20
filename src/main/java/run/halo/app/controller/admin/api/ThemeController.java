package run.halo.app.controller.admin.api;

import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import run.halo.app.handler.theme.config.support.Group;
import run.halo.app.handler.theme.config.support.ThemeProperty;
import run.halo.app.model.params.ThemeContentParam;
import run.halo.app.model.support.BaseResponse;
import run.halo.app.model.support.ThemeFile;
import run.halo.app.service.ThemeService;
import run.halo.app.service.ThemeSettingService;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Theme controller.
 *
 * @author ryanwang
 * @date : 2019/3/20
 */
@RestController
@RequestMapping("/api/admin/themes")
public class ThemeController {

    private final ThemeService themeService;

    private final ThemeSettingService themeSettingService;

    public ThemeController(ThemeService themeService,
                           ThemeSettingService themeSettingService) {
        this.themeService = themeService;
        this.themeSettingService = themeSettingService;
    }

    @GetMapping("{themeId}")
    @ApiOperation("Gets theme property by theme id")
    public ThemeProperty getBy(@PathVariable("themeId") String themeId) {
        return themeService.getThemeOfNonNullBy(themeId);
    }

    @GetMapping
    @ApiOperation("List all themes")
    public Set<ThemeProperty> listAll() {
        return themeService.getThemes();
    }

    @GetMapping("activation/files")
    public List<ThemeFile> listFiles() {
        return themeService.listThemeFolderBy(themeService.getActivatedThemeId());
    }

    @GetMapping("{themeId}/files")
    public List<ThemeFile> listFiles(@PathVariable("themeId") String themeId) {
        return themeService.listThemeFolderBy(themeId);
    }

    @GetMapping("files/content")
    public BaseResponse<String> getContentBy(@RequestParam(name = "path") String path) {
        return BaseResponse.ok(HttpStatus.OK.getReasonPhrase(), themeService.getTemplateContent(path));
    }

    @GetMapping("{themeId}/files/content")
    public BaseResponse<String> getContentBy(@PathVariable("themeId") String themeId,
                                             @RequestParam(name = "path") String path) {
        return BaseResponse.ok(HttpStatus.OK.getReasonPhrase(), themeService.getTemplateContent(themeId, path));
    }

    @PutMapping("files/content")
    public void updateContentBy(@RequestBody ThemeContentParam param) {
        themeService.saveTemplateContent(param.getPath(), param.getContent());
    }

    @PutMapping("{themeId}/files/content")
    public void updateContentBy(@PathVariable("themeId") String themeId,
                                @RequestBody ThemeContentParam param) {
        themeService.saveTemplateContent(themeId, param.getPath(), param.getContent());
    }

    @GetMapping("activation/template/custom/sheet")
    public Set<String> customSheetTemplate() {
        return themeService.listCustomTemplates(themeService.getActivatedThemeId(), ThemeService.CUSTOM_SHEET_PREFIX);
    }

    @GetMapping("activation/template/custom/post")
    public Set<String> customPostTemplate() {
        return themeService.listCustomTemplates(themeService.getActivatedThemeId(), ThemeService.CUSTOM_POST_PREFIX);
    }

    @PostMapping("{themeId}/activation")
    @ApiOperation("Activates a theme")
    public ThemeProperty active(@PathVariable("themeId") String themeId) {
        return themeService.activateTheme(themeId);
    }

    @GetMapping("activation")
    @ApiOperation("Gets activate theme")
    public ThemeProperty getActivateTheme() {
        return themeService.getThemeOfNonNullBy(themeService.getActivatedThemeId());
    }

    @GetMapping("activation/configurations")
    @ApiOperation("Fetches activated theme configuration")
    public BaseResponse<Object> fetchConfig() {
        return BaseResponse.ok(themeService.fetchConfig(themeService.getActivatedThemeId()));
    }

    @GetMapping("{themeId}/configurations")
    @ApiOperation("Fetches theme configuration by theme id")
    public List<Group> fetchConfig(@PathVariable("themeId") String themeId) {
        return themeService.fetchConfig(themeId);
    }

    @GetMapping("activation/settings")
    @ApiOperation("Lists activated theme settings")
    public Map<String, Object> listSettingsBy() {
        return themeSettingService.listAsMapBy(themeService.getActivatedThemeId());
    }

    @GetMapping("{themeId}/settings")
    @ApiOperation("Lists theme settings by theme id")
    public Map<String, Object> listSettingsBy(@PathVariable("themeId") String themeId) {
        return themeSettingService.listAsMapBy(themeId);
    }

    @PostMapping("activation/settings")
    @ApiOperation("Saves theme settings")
    public void saveSettingsBy(@RequestBody Map<String, Object> settings) {
        themeSettingService.save(settings, themeService.getActivatedThemeId());
    }

    @PostMapping("{themeId}/settings")
    @ApiOperation("Saves theme settings")
    public void saveSettingsBy(@PathVariable("themeId") String themeId,
                               @RequestBody Map<String, Object> settings) {
        themeSettingService.save(settings, themeId);
    }

    @DeleteMapping("{themeId}")
    @ApiOperation("Deletes a theme")
    public void deleteBy(@PathVariable("themeId") String themeId) {
        themeService.deleteTheme(themeId);
    }

    @PostMapping("upload")
    @ApiOperation("Upload theme")
    public ThemeProperty uploadTheme(@RequestPart("file") MultipartFile file) {
        return themeService.upload(file);
    }

    @PutMapping("upload/{themeId}")
    public ThemeProperty updateThemeByUpload(@PathVariable("themeId") String themeId,
                                             @RequestPart("file") MultipartFile file) {
        return themeService.update(themeId, file);
    }

    @PostMapping("fetching")
    @ApiOperation("Fetches a new theme")
    public ThemeProperty fetchTheme(@RequestParam("uri") String uri) {
        return themeService.fetch(uri);
    }

    @PutMapping("fetching/{themeId}")
    public ThemeProperty updateThemeByFetching(@PathVariable("themeId") String themeId,
                                               @RequestPart(name = "file", required = false) MultipartFile file) {

        return themeService.update(themeId);
    }

    @PostMapping("reload")
    @ApiOperation("Reloads themes")
    public void reload() {
        themeService.reload();
    }

    @GetMapping(value = "activation/template/exists")
    public BaseResponse exists(@RequestParam(value = "template") String template) {
        return BaseResponse.ok(themeService.templateExists(template));
    }

}
