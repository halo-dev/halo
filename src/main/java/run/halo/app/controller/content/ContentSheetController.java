package run.halo.app.controller.content;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import run.halo.app.cache.StringCacheStore;
import run.halo.app.exception.ForbiddenException;
import run.halo.app.model.entity.Sheet;
import run.halo.app.model.enums.PostStatus;
import run.halo.app.model.support.HaloConst;
import run.halo.app.service.SheetService;
import run.halo.app.service.ThemeService;
import run.halo.app.utils.MarkdownUtils;

import java.util.concurrent.TimeUnit;

/**
 * Content sheet controller.
 *
 * @author ryanwang
 * @date : 2019-03-21
 */
@Controller
public class ContentSheetController {


    private final SheetService sheetService;

    private final ThemeService themeService;

    private final StringCacheStore cacheStore;

    public ContentSheetController(SheetService sheetService,
                                  ThemeService themeService,
                                  StringCacheStore cacheStore) {
        this.sheetService = sheetService;
        this.themeService = themeService;
        this.cacheStore = cacheStore;
    }

    /**
     * Render photo page
     *
     * @return template path: themes/{theme}/photos.ftl
     */
    @GetMapping(value = "/photos")
    public String photos() {
        return themeService.render("photos");
    }

    /**
     * Render links page
     *
     * @return template path: themes/{theme}/links.ftl
     */
    @GetMapping(value = "/links")
    public String links() {
        return themeService.render("links");
    }

    /**
     * Render custom sheet
     *
     * @param url     sheet url
     * @param preview preview
     * @param token   token
     * @param model   model
     * @return template path: themes/{theme}/sheet.ftl
     */
    @GetMapping(value = "/s/{url}")
    public String sheet(@PathVariable(value = "url") String url,
                        @RequestParam(value = "preview", required = false, defaultValue = "false") boolean preview,
                        @RequestParam(value = "token", required = false) String token,
                        Model model) {
        Sheet sheet = sheetService.getBy(preview ? PostStatus.DRAFT : PostStatus.PUBLISHED, url);

        if (preview) {
            // render markdown to html when preview post
            sheet.setFormatContent(MarkdownUtils.renderHtml(sheet.getOriginalContent()));

            // verify token
            String cachedToken = cacheStore.getAny("preview-sheet-token-" + sheet.getId(), String.class).orElseThrow(() -> new ForbiddenException("该页面的预览链接不存在或已过期"));

            if (!cachedToken.equals(token)) {
                throw new ForbiddenException("该页面的预览链接不存在或已过期");
            }
        }

        // sheet and post all can use
        model.addAttribute("sheet", sheetService.convertToDetail(sheet));
        model.addAttribute("post", sheetService.convertToDetail(sheet));
        model.addAttribute("is_sheet", true);

        if (preview) {
            // refresh timeUnit
            cacheStore.putAny("preview-sheet-token-" + sheet.getId(), token, 10, TimeUnit.MINUTES);
        }

        if (themeService.templateExists(ThemeService.CUSTOM_SHEET_PREFIX + sheet.getTemplate() + HaloConst.SUFFIX_FTL)) {
            return themeService.render(ThemeService.CUSTOM_SHEET_PREFIX + sheet.getTemplate());
        }
        return themeService.render("sheet");
    }
}
