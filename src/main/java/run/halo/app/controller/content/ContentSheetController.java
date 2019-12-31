package run.halo.app.controller.content;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import run.halo.app.cache.StringCacheStore;
import run.halo.app.exception.ForbiddenException;
import run.halo.app.model.dto.PhotoDTO;
import run.halo.app.model.entity.Sheet;
import run.halo.app.model.enums.PostStatus;
import run.halo.app.model.support.HaloConst;
import run.halo.app.model.vo.BaseCommentVO;
import run.halo.app.model.vo.SheetDetailVO;
import run.halo.app.service.*;
import run.halo.app.utils.MarkdownUtils;

import static org.springframework.data.domain.Sort.Direction.DESC;

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

    private final SheetCommentService sheetCommentService;

    private final PhotoService photoService;

    private final OptionService optionService;

    private final StringCacheStore cacheStore;

    public ContentSheetController(SheetService sheetService,
                                  ThemeService themeService,
                                  SheetCommentService sheetCommentService,
                                  PhotoService photoService,
                                  OptionService optionService,
                                  StringCacheStore cacheStore) {
        this.sheetService = sheetService;
        this.themeService = themeService;
        this.sheetCommentService = sheetCommentService;
        this.photoService = photoService;
        this.optionService = optionService;
        this.cacheStore = cacheStore;
    }

    /**
     * Render photo page
     *
     * @return template path: themes/{theme}/photos.ftl
     */
    @GetMapping(value = "/photos")
    public String photos(Model model,
                         @RequestParam(value = "size", required = false, defaultValue = "10") Integer size) {
        return photos(model, 1, size);
    }

    /**
     * Render photo page
     *
     * @param model model
     * @param page  current page
     * @param size  current page size
     * @return template path: themes/{theme}/photos.ftl
     */
    @GetMapping(value = "/photos/page/{page}")
    public String photos(Model model,
                         @PathVariable(value = "page") Integer page,
                         @RequestParam(value = "size", required = false, defaultValue = "10") Integer size) {
        Pageable pageable = PageRequest.of(page >= 1 ? page - 1 : page, size, Sort.by(DESC, "createTime"));
        Page<PhotoDTO> photos = photoService.pageBy(pageable);
        model.addAttribute("photos", photos);
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
                        @RequestParam(value = "token", required = false) String token,
                        @RequestParam(value = "cp", defaultValue = "1") Integer cp,
                        @SortDefault(sort = "createTime", direction = DESC) Sort sort,
                        Model model) {

        Sheet sheet = sheetService.getByUrl(url);

        if (StringUtils.isEmpty(token)) {
            sheet = sheetService.getBy(PostStatus.PUBLISHED, url);
        } else {
            // render markdown to html when preview sheet
            sheet.setFormatContent(MarkdownUtils.renderHtml(sheet.getOriginalContent()));

            // verify token
            String cachedToken = cacheStore.getAny(token, String.class).orElseThrow(() -> new ForbiddenException("您没有该页面的访问权限"));

            if (!cachedToken.equals(token)) {
                throw new ForbiddenException("您没有该页面的访问权限");
            }
        }

        Page<BaseCommentVO> comments = sheetCommentService.pageVosBy(sheet.getId(), PageRequest.of(cp, optionService.getCommentPageSize(), sort));

        SheetDetailVO sheetDetailVO = sheetService.convertToDetailVo(sheet);

        // sheet and post all can use
        model.addAttribute("sheet", sheetDetailVO);
        model.addAttribute("post", sheetDetailVO);
        model.addAttribute("is_sheet", true);
        model.addAttribute("comments", comments);

        if (themeService.templateExists(ThemeService.CUSTOM_SHEET_PREFIX + sheet.getTemplate() + HaloConst.SUFFIX_FTL)) {
            return themeService.render(ThemeService.CUSTOM_SHEET_PREFIX + sheet.getTemplate());
        }
        return themeService.render("sheet");
    }
}
