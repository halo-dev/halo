package cc.ryanc.halo.web.controller.admin;

import cc.ryanc.halo.logging.Logger;
import cc.ryanc.halo.model.support.JsonResult;
import cc.ryanc.halo.service.LogService;
import cc.ryanc.halo.utils.ThemeUtils;
import cc.ryanc.halo.web.controller.admin.base.BaseController;
import cc.ryanc.halo.web.controller.content.base.BaseContentController;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileReader;
import cn.hutool.core.io.file.FileWriter;
import cn.hutool.core.text.StrBuilder;
import cn.hutool.core.util.RuntimeUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.ZipUtil;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.List;

import static cc.ryanc.halo.model.support.HaloConst.THEMES;

/**
 * Themes controller
 *
 * @author : RYAN0UP
 * @date : 2019-03-14
 */
@Controller
@RequestMapping(value = "/admin/themes")
public class ThemeController extends BaseController {

    private final Logger log = Logger.getLogger(getClass());

    private LogService logsService;

    public ThemeController(LogService logsService) {
        this.logsService = logsService;
    }

    /**
     * Render theme manage
     *
     * @param model model
     * @return template path: admin/admin_theme.ftl
     */
    @GetMapping
    public String themes(Model model) {
        model.addAttribute("activeTheme", BaseContentController.THEME);
        model.addAttribute("themes", THEMES);
        return "admin/admin_theme";
    }

    /**
     * Active theme
     *
     * @param themeName theme name
     * @param request   request
     * @return JsonResult
     */
    @PostMapping(value = "/active")
    @ResponseBody
    @CacheEvict(value = "posts", allEntries = true, beforeInvocation = true)
    public JsonResult activeTheme(@RequestParam("themeName") String themeName,
                                  HttpServletRequest request) {
        try {
            optionService.saveOption("theme", themeName);
            BaseContentController.THEME = themeName;
            configuration.setSharedVariable("themeName", themeName);
            log.info("Changed theme to {}", themeName);
            return new JsonResult(1, localeMessage("code.admin.theme.change-success", new Object[]{themeName}));
        } catch (Exception e) {
            return new JsonResult(0, localeMessage("code.admin.theme.change-failed"));
        } finally {
            refreshCache();
        }
    }


    /**
     * Upload theme
     *
     * @param file file
     * @return JsonResult
     */
    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult uploadTheme(@RequestParam("file") MultipartFile file,
                                  HttpServletRequest request) {
        try {
            if (!file.isEmpty()) {
                final File themePath = new File(ThemeUtils.getUsersThemesPath().getAbsolutePath(), file.getOriginalFilename());
                file.transferTo(themePath);
                ZipUtil.unzip(themePath, ThemeUtils.getUsersThemesPath());
                FileUtil.del(themePath);
                log.info("Upload topic success, path is " + themePath.getAbsolutePath());
//                logsService.save(LogsRecord.UPLOAD_THEME, file.getOriginalFilename(), request);
            } else {
                log.error("Upload theme failed, no file selected");
                return new JsonResult(0, localeMessage("code.admin.theme.upload-no-file"));
            }
        } catch (Exception e) {
            log.error("Upload theme failed: {}", e.getMessage());
            return new JsonResult(0, localeMessage("code.admin.theme.upload-failed"));
        } finally {
            refreshCache();
        }
        return new JsonResult(1, localeMessage("code.admin.theme.upload-success"));
    }

    /**
     * Delete theme
     *
     * @param themeName theme name
     * @return redirect to admin/themes
     */
    @GetMapping(value = "/remove")
    public String removeTheme(@RequestParam("themeName") String themeName) {
        try {
            final File themePath = new File(ThemeUtils.getThemesPath(themeName), themeName);
            FileUtil.del(themePath);
        } catch (Exception e) {
            log.error("Delete theme failed: {}", e.getMessage());
        } finally {
            refreshCache();
        }
        return "redirect:/admin/themes";
    }

    /**
     * Install theme page
     *
     * @return template path: admin/widget/_theme-install.ftl
     */
    @GetMapping(value = "/install")
    public String install() {
        return "admin/widget/_theme-install";
    }

    /**
     * Clone theme by git
     *
     * @param remoteAddr theme remote address
     * @param themeName  theme name
     * @return JsonResult
     */
    @PostMapping(value = "/clone")
    @ResponseBody
    public JsonResult cloneFromRemote(@RequestParam(value = "remoteAddr") String remoteAddr,
                                      @RequestParam(value = "themeName") String themeName) {
        if (StrUtil.isBlank(remoteAddr) || StrUtil.isBlank(themeName)) {
            return new JsonResult(0, localeMessage("code.admin.common.info-no-complete"));
        }
        try {
            final String cmdResult = RuntimeUtil.execForStr("git clone " + remoteAddr + " " + ThemeUtils.getUsersThemesPath().getAbsolutePath() + "/" + themeName);
            if ("".equals(cmdResult)) {
                return new JsonResult(0, localeMessage("code.admin.theme.no-git"));
            }
        } catch (Exception e) {
            log.error("Cloning theme failed: {}", e.getMessage());
            return new JsonResult(0, localeMessage("code.admin.theme.clone-theme-failed"));
        } finally {
            refreshCache();
        }
        return new JsonResult(1, localeMessage("code.admin.common.install-success"));
    }

    /**
     * Update theme
     *
     * @param themeName theme name
     * @return JsonResult
     */
    @GetMapping(value = "/pull")
    @ResponseBody
    public JsonResult pullFromRemote(@RequestParam(value = "themeName") String themeName) {
        try {
            final String cmdResult = RuntimeUtil.execForStr("cd " + ThemeUtils.getUsersThemesPath().getAbsolutePath() + "/" + themeName + " && git pull");
            if ("".equals(cmdResult)) {
                return new JsonResult(0, localeMessage("code.admin.theme.no-git"));
            }
        } catch (Exception e) {
            log.error("Update theme failed: {}", e.getMessage());
            return new JsonResult(0, localeMessage("code.admin.theme.update-theme-failed"));
        } finally {
            refreshCache();
        }
        return new JsonResult(1, localeMessage("code.admin.common.update-success"));
    }

    /**
     * Redirect to theme option page
     *
     * @param model     model
     * @param theme     theme name
     * @param hasUpdate hasUpdate
     */
    @GetMapping(value = "/options")
    public String options(Model model,
                          @RequestParam("theme") String theme,
                          @RequestParam("hasUpdate") String hasUpdate) {
        model.addAttribute("themeDir", theme);
        if (StrUtil.equals(hasUpdate, "true")) {
            model.addAttribute("hasUpdate", true);
        } else {
            model.addAttribute("hasUpdate", false);
        }
        return "themes/" + theme + "/module/options";
    }

    /**
     * Edit theme template
     *
     * @param model model
     * @return template path: admin/admin_theme-editor.ftl
     */
    @GetMapping(value = "/editor")
    public String editor(Model model) {
        final List<String> templates = ThemeUtils.getTplName(BaseContentController.THEME);
        model.addAttribute("tpls", templates);
        return "admin/admin_theme-editor";
    }

    /**
     * Get template content
     *
     * @param tplName template name
     * @return template content
     */
    @GetMapping(value = "/getTpl", produces = "text/text;charset=UTF-8")
    @ResponseBody
    public String getTplContent(@RequestParam("tplName") String tplName) {
        String tplContent = "";
        try {
            final StrBuilder themePath = new StrBuilder(ThemeUtils.getThemesPath(BaseContentController.THEME).getAbsolutePath());
            themePath.append(BaseContentController.THEME);
            themePath.append("/");
            themePath.append(tplName);
            final File themesPath = new File(themePath.toString());
            final FileReader fileReader = new FileReader(themesPath);
            tplContent = fileReader.readString();
        } catch (Exception e) {
            log.error("Get template file error: {}", e.getMessage());
        }
        return tplContent;
    }

    /**
     * Save template file
     *
     * @param tplName    template name
     * @param tplContent template content
     * @return JsonResult
     */
    @PostMapping(value = "/editor/save")
    @ResponseBody
    public JsonResult saveTpl(@RequestParam("tplName") String tplName,
                              @RequestParam("tplContent") String tplContent) {
        if (StrUtil.isBlank(tplContent)) {
            return new JsonResult(0, localeMessage("code.admin.theme.edit.no-content"));
        }
        try {
            final StrBuilder themePath = new StrBuilder(ThemeUtils.getThemesPath(BaseContentController.THEME).getAbsolutePath());
            themePath.append(BaseContentController.THEME);
            themePath.append("/");
            themePath.append(tplName);
            final File tplPath = new File(themePath.toString());
            final FileWriter fileWriter = new FileWriter(tplPath);
            fileWriter.write(tplContent);
        } catch (Exception e) {
            log.error("Template save failed: {}", e.getMessage());
            return new JsonResult(0, localeMessage("code.admin.common.save-failed"));
        }
        return new JsonResult(1, localeMessage("code.admin.common.save-success"));
    }
}
