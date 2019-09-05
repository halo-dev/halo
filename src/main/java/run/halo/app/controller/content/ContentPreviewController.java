package run.halo.app.controller.content;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import run.halo.app.cache.StringCacheStore;
import run.halo.app.exception.ForbiddenException;
import run.halo.app.model.entity.Category;
import run.halo.app.model.entity.Post;
import run.halo.app.model.entity.Sheet;
import run.halo.app.model.entity.Tag;
import run.halo.app.model.enums.PostStatus;
import run.halo.app.model.support.HaloConst;
import run.halo.app.service.*;
import run.halo.app.utils.MarkdownUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Post and sheet preview controller.
 *
 * @author ryanwang
 * @date : 2019-09-05
 */
@Slf4j
@Controller
@RequestMapping(value = "/preview")
public class ContentPreviewController {

    private final PostService postService;

    private final ThemeService themeService;

    private final PostCategoryService postCategoryService;

    private final PostTagService postTagService;

    private final SheetService sheetService;

    private final StringCacheStore cacheStore;

    public ContentPreviewController(PostService postService,
                                    ThemeService themeService,
                                    PostCategoryService postCategoryService,
                                    PostTagService postTagService,
                                    SheetService sheetService,
                                    StringCacheStore cacheStore) {
        this.postService = postService;
        this.themeService = themeService;
        this.postCategoryService = postCategoryService;
        this.postTagService = postTagService;
        this.sheetService = sheetService;
        this.cacheStore = cacheStore;
    }

    @GetMapping(value = "post/{url}")
    public String post(@PathVariable("url") String url,
                       @RequestParam(value = "token") String token,
                       Model model) {
        Post post = postService.getBy(PostStatus.DRAFT, url);

        post.setFormatContent(MarkdownUtils.renderHtml(post.getOriginalContent()));

        // verify token
        String cachedToken = cacheStore.getAny("preview-post-token-" + post.getId(), String.class).orElseThrow(() -> new ForbiddenException("该文章的预览链接不存在或已过期"));

        if (!cachedToken.equals(token)) {
            throw new ForbiddenException("该文章的预览链接不存在或已过期");
        }

        List<Category> categories = postCategoryService.listCategoriesBy(post.getId());
        List<Tag> tags = postTagService.listTagsBy(post.getId());

        model.addAttribute("is_post", true);
        model.addAttribute("post", post);
        model.addAttribute("categories", categories);
        model.addAttribute("tags", tags);

        // refresh timeUnit
        cacheStore.putAny("preview-post-token-" + post.getId(), token, 10, TimeUnit.MINUTES);

        return themeService.render("post");
    }

    @GetMapping(value = "s/{url}")
    public String sheet(@PathVariable("url") String url,
                        @RequestParam(value = "token") String token,
                        Model model) {

        Sheet sheet = sheetService.getBy(PostStatus.DRAFT, url);

        sheet.setFormatContent(MarkdownUtils.renderHtml(sheet.getOriginalContent()));

        // verify token
        String cachedToken = cacheStore.getAny("preview-sheet-token-" + sheet.getId(), String.class).orElseThrow(() -> new ForbiddenException("该页面的预览链接不存在或已过期"));

        if (!cachedToken.equals(token)) {
            throw new ForbiddenException("该页面的预览链接不存在或已过期");
        }

        // sheet and post all can use
        model.addAttribute("sheet", sheetService.convertToDetail(sheet));
        model.addAttribute("post", sheetService.convertToDetail(sheet));
        model.addAttribute("is_sheet", true);

        if (themeService.templateExists(ThemeService.CUSTOM_SHEET_PREFIX + sheet.getTemplate() + HaloConst.SUFFIX_FTL)) {
            return themeService.render(ThemeService.CUSTOM_SHEET_PREFIX + sheet.getTemplate());
        }
        return themeService.render("sheet");
    }
}
