package run.halo.app.controller.content.model;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import run.halo.app.cache.StringCacheStore;
import run.halo.app.exception.ForbiddenException;
import run.halo.app.model.entity.Sheet;
import run.halo.app.model.support.HaloConst;
import run.halo.app.model.vo.SheetDetailVO;
import run.halo.app.service.SheetService;
import run.halo.app.service.ThemeService;
import run.halo.app.utils.MarkdownUtils;

/**
 * Sheet model.
 *
 * @author ryanwang
 * @date 2020-01-07
 */
@Component
public class SheetModel {

    private final SheetService sheetService;

    private final StringCacheStore cacheStore;

    private final ThemeService themeService;

    public SheetModel(SheetService sheetService, StringCacheStore cacheStore, ThemeService themeService) {
        this.sheetService = sheetService;
        this.cacheStore = cacheStore;
        this.themeService = themeService;
    }

    public String content(Sheet sheet, String token, Model model) {
        if (!StringUtils.isEmpty(token)) {
            // render markdown to html when preview sheet
            sheet.setFormatContent(MarkdownUtils.renderHtml(sheet.getOriginalContent()));

            // verify token
            String cachedToken = cacheStore.getAny(token, String.class).orElseThrow(() -> new ForbiddenException("您没有该页面的访问权限"));

            if (!cachedToken.equals(token)) {
                throw new ForbiddenException("您没有该页面的访问权限");
            }
        }
        sheetService.publishVisitEvent(sheet.getId());

        SheetDetailVO sheetDetailVO = sheetService.convertToDetailVo(sheet);

        // sheet and post all can use
        model.addAttribute("sheet", sheetDetailVO);
        model.addAttribute("post", sheetDetailVO);
        model.addAttribute("is_sheet", true);

        // TODO,Will be deprecated
        model.addAttribute("comments", Page.empty());

        if (themeService.templateExists(ThemeService.CUSTOM_SHEET_PREFIX + sheet.getTemplate() + HaloConst.SUFFIX_FTL)) {
            return themeService.render(ThemeService.CUSTOM_SHEET_PREFIX + sheet.getTemplate());
        }
        return themeService.render("sheet");
    }
}
