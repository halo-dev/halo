package run.halo.app.controller.admin.api;

import io.swagger.annotations.ApiOperation;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import run.halo.app.annotation.DisableOnCondition;
import run.halo.app.cache.lock.CacheLock;
import run.halo.app.handler.theme.config.support.Group;
import run.halo.app.handler.theme.config.support.ThemeProperty;
import run.halo.app.model.params.ThemeContentParam;
import run.halo.app.model.support.BaseResponse;
import run.halo.app.model.support.ThemeFile;
import run.halo.app.service.ThemeService;
import run.halo.app.service.ThemeSettingService;

/**
 * Theme controller.
 *
 * @author ryanwang
 * @date 2019-03-20
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

    @GetMapping("{themeId:.+}")
    @ApiOperation("Gets theme property by theme id")
    public ThemeProperty getBy(@PathVariable("themeId") String themeId) {
        return themeService.getThemeOfNonNullBy(themeId);
    }

    @GetMapping
    @ApiOperation("Lists all themes")
    public List<ThemeProperty> listAll() {
        return themeService.getThemes();
    }

    @GetMapping("activation/files")
    @ApiOperation("Lists all activate theme files")
    public List<ThemeFile> listFiles() {
        return themeService.listThemeFolderBy(themeService.getActivatedThemeId());
    }

    @GetMapping("{themeId:.+}/files")
    @ApiOperation("Lists theme files by theme id")
    public List<ThemeFile> listFiles(@PathVariable("themeId") String themeId) {
        return themeService.listThemeFolderBy(themeId);
    }

    @GetMapping("files/content")
    @ApiOperation("Gets template content")
    public BaseResponse<String> getContentBy(@RequestParam(name = "path") String path) {
        return BaseResponse
            .ok(HttpStatus.OK.getReasonPhrase(), themeService.getTemplateContent(path));
    }

    @GetMapping("{themeId:.+}/files/content")
    @ApiOperation("Gets template content by theme id")
    public BaseResponse<String> getContentBy(@PathVariable("themeId") String themeId,
        @RequestParam(name = "path") String path) {
        return BaseResponse
            .ok(HttpStatus.OK.getReasonPhrase(), themeService.getTemplateContent(themeId, path));
    }

    @PutMapping("files/content")
    @ApiOperation("Updates template content")
    @DisableOnCondition
    public void updateContentBy(@RequestBody ThemeContentParam param) {
        themeService.saveTemplateContent(param.getPath(), param.getContent());
    }

    @PutMapping("{themeId:.+}/files/content")
    @ApiOperation("Updates template content by theme id")
    @DisableOnCondition
    public void updateContentBy(@PathVariable("themeId") String themeId,
        @RequestBody ThemeContentParam param) {
        themeService.saveTemplateContent(themeId, param.getPath(), param.getContent());
    }

    @GetMapping("activation/template/custom/sheet")
    @ApiOperation("Gets custom sheet templates")
    public List<String> customSheetTemplate() {
        return themeService.listCustomTemplates(themeService.getActivatedThemeId(),
            ThemeService.CUSTOM_SHEET_PREFIX);
    }

    @GetMapping("activation/template/custom/post")
    @ApiOperation("Gets custom post templates")
    public List<String> customPostTemplate() {
        return themeService.listCustomTemplates(themeService.getActivatedThemeId(),
            ThemeService.CUSTOM_POST_PREFIX);
    }

    @PostMapping("{themeId:.+}/activation")
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

    @GetMapping("{themeId:.+}/configurations")
    @ApiOperation("Fetches theme configuration by theme id")
    public List<Group> fetchConfig(@PathVariable("themeId") String themeId) {
        return themeService.fetchConfig(themeId);
    }

    @GetMapping("activation/settings")
    @ApiOperation("Lists activated theme settings")
    public Map<String, Object> listSettingsBy() {
        return themeSettingService.listAsMapBy(themeService.getActivatedThemeId());
    }

    @GetMapping("{themeId:.+}/settings")
    @ApiOperation("Lists theme settings by theme id")
    public Map<String, Object> listSettingsBy(@PathVariable("themeId") String themeId) {
        return themeSettingService.listAsMapBy(themeId);
    }

    @PostMapping("activation/settings")
    @ApiOperation("Saves theme settings")
    public void saveSettingsBy(@RequestBody Map<String, Object> settings) {
        themeSettingService.save(settings, themeService.getActivatedThemeId());
    }

    @PostMapping("{themeId:.+}/settings")
    @ApiOperation("Saves theme settings")
    @CacheLock(prefix = "save_theme_setting_by_themeId")
    public void saveSettingsBy(@PathVariable("themeId") String themeId,
        @RequestBody Map<String, Object> settings) {
        themeSettingService.save(settings, themeId);
    }

    @DeleteMapping("{themeId:.+}")
    @ApiOperation("Deletes a theme")
    @DisableOnCondition
    public void deleteBy(@PathVariable("themeId") String themeId,
        @RequestParam(value = "deleteSettings", defaultValue = "false") Boolean deleteSettings) {
        themeService.deleteTheme(themeId, deleteSettings);
    }

    @PostMapping("upload")
    @ApiOperation("Uploads a theme")
    public ThemeProperty uploadTheme(@RequestPart("file") MultipartFile file) {
        return themeService.upload(file);
    }

    @PutMapping("upload/{themeId:.+}")
    @ApiOperation("Upgrades theme by file")
    public ThemeProperty updateThemeByUpload(@PathVariable("themeId") String themeId,
        @RequestPart("file") MultipartFile file) {
        return themeService.update(themeId, file);
    }

    @PostMapping("fetching")
    @ApiOperation("Fetches a new theme")
    public ThemeProperty fetchTheme(@RequestParam("uri") String uri) {
        return themeService.fetch(uri);
    }

    @PostMapping(value = {"fetchingBranches", "/fetching/git/branches"})
    @ApiOperation("Fetches all branches")
    @Deprecated(since = "1.4.2", forRemoval = true)
    public List<ThemeProperty> fetchBranches(@RequestParam("uri") String uri) {
        return themeService.fetchBranches(uri);
    }

    @PostMapping("fetchingReleases")
    @ApiOperation("Fetches all releases")
    @Deprecated(since = "1.4.2", forRemoval = true)
    public List<ThemeProperty> fetchReleases(@RequestParam("uri") String uri) {
        return themeService.fetchReleases(uri);
    }

    @GetMapping("fetchingRelease")
    @ApiOperation("Fetches a specific release")
    @Deprecated(since = "1.4.2", forRemoval = true)
    public ThemeProperty fetchRelease(@RequestParam("uri") String uri,
        @RequestParam("tag") String tagName) {
        return themeService.fetchRelease(uri, tagName);
    }

    @GetMapping("fetchBranch")
    @ApiOperation("Fetch specific branch")
    @Deprecated(since = "1.4.2", forRemoval = true)
    public ThemeProperty fetchBranch(@RequestParam("uri") String uri,
        @RequestParam("branch") String branchName) {
        return themeService.fetchBranch(uri, branchName);
    }

    @GetMapping("fetchLatestRelease")
    @ApiOperation("Fetch latest release")
    @Deprecated(since = "1.4.2", forRemoval = true)
    public ThemeProperty fetchLatestRelease(@RequestParam("uri") String uri) {
        return themeService.fetchLatestRelease(uri);
    }

    @PutMapping("fetching/{themeId:.+}")
    @ApiOperation("Upgrades theme from remote")
    public ThemeProperty updateThemeByFetching(@PathVariable("themeId") String themeId) {
        return themeService.update(themeId);
    }

    @PostMapping("reload")
    @ApiOperation("Reloads themes")
    public void reload() {
        themeService.reload();
    }

    @GetMapping(value = "activation/template/exists")
    @ApiOperation("Determines if template exists")
    public BaseResponse<Boolean> exists(@RequestParam(value = "template") String template) {
        return BaseResponse.ok(themeService.templateExists(template));
    }
}
