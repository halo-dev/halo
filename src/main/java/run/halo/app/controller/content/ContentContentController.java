package run.halo.app.controller.content;

import static run.halo.app.model.support.HaloConst.POST_PASSWORD_TEMPLATE;
import static run.halo.app.model.support.HaloConst.SUFFIX_FTL;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import run.halo.app.cache.lock.CacheLock;
import run.halo.app.controller.content.auth.ContentAuthenticationManager;
import run.halo.app.controller.content.auth.ContentAuthenticationRequest;
import run.halo.app.controller.content.model.CategoryModel;
import run.halo.app.controller.content.model.JournalModel;
import run.halo.app.controller.content.model.LinkModel;
import run.halo.app.controller.content.model.PhotoModel;
import run.halo.app.controller.content.model.PostModel;
import run.halo.app.controller.content.model.SheetModel;
import run.halo.app.controller.content.model.TagModel;
import run.halo.app.exception.AuthenticationException;
import run.halo.app.exception.NotFoundException;
import run.halo.app.exception.UnsupportedException;
import run.halo.app.model.dto.CategoryDTO;
import run.halo.app.model.dto.post.BasePostMinimalDTO;
import run.halo.app.model.entity.Category;
import run.halo.app.model.entity.Post;
import run.halo.app.model.entity.Sheet;
import run.halo.app.model.enums.EncryptTypeEnum;
import run.halo.app.model.enums.PostPermalinkType;
import run.halo.app.model.enums.PostStatus;
import run.halo.app.model.enums.SheetPermalinkType;
import run.halo.app.service.CategoryService;
import run.halo.app.service.OptionService;
import run.halo.app.service.PostService;
import run.halo.app.service.SheetService;
import run.halo.app.service.ThemeService;
import run.halo.app.service.assembler.PostRenderAssembler;

/**
 * @author ryanwang
 * @author guqing
 * @date 2020-01-07
 */
@Slf4j
@Controller
@RequestMapping
public class ContentContentController {

    private final PostModel postModel;

    private final SheetModel sheetModel;

    private final CategoryModel categoryModel;

    private final TagModel tagModel;

    private final JournalModel journalModel;

    private final PhotoModel photoModel;

    private final LinkModel linkModel;

    private final OptionService optionService;

    private final PostService postService;

    private final SheetService sheetService;

    private final CategoryService categoryService;

    private final ThemeService themeService;

    private final PostRenderAssembler postRenderAssembler;

    private final ContentAuthenticationManager providerManager;

    public ContentContentController(PostModel postModel,
        SheetModel sheetModel,
        CategoryModel categoryModel,
        TagModel tagModel,
        JournalModel journalModel,
        PhotoModel photoModel,
        LinkModel linkModel,
        OptionService optionService,
        PostService postService,
        SheetService sheetService,
        CategoryService categoryService,
        ThemeService themeService,
        PostRenderAssembler postRenderAssembler,
        ContentAuthenticationManager providerManager) {
        this.postModel = postModel;
        this.sheetModel = sheetModel;
        this.categoryModel = categoryModel;
        this.tagModel = tagModel;
        this.journalModel = journalModel;
        this.photoModel = photoModel;
        this.linkModel = linkModel;
        this.optionService = optionService;
        this.postService = postService;
        this.sheetService = sheetService;
        this.categoryService = categoryService;
        this.themeService = themeService;
        this.postRenderAssembler = postRenderAssembler;
        this.providerManager = providerManager;
    }

    @GetMapping("{prefix}")
    public String content(@PathVariable("prefix") String prefix,
        @RequestParam(value = "token", required = false) String token,
        Model model) {
        if (optionService.getArchivesPrefix().equals(prefix)) {
            return postModel.archives(1, model);
        }
        if (optionService.getCategoriesPrefix().equals(prefix)) {
            return categoryModel.list(model);
        }
        if (optionService.getTagsPrefix().equals(prefix)) {
            return tagModel.list(model);
        }
        if (optionService.getJournalsPrefix().equals(prefix)) {
            return journalModel.list(1, model);
        }
        if (optionService.getPhotosPrefix().equals(prefix)) {
            return photoModel.list(1, model);
        }
        if (optionService.getLinksPrefix().equals(prefix)) {
            return linkModel.list(model);
        }
        if (optionService.getSheetPermalinkType().equals(SheetPermalinkType.ROOT)) {
            Sheet sheet = sheetService.getBySlug(prefix);
            return sheetModel.content(sheet, token, model);
        }

        throw buildPathNotFoundException();
    }

    @GetMapping("{prefix}/page/{page:\\d+}")
    public String content(@PathVariable("prefix") String prefix,
        @PathVariable(value = "page") Integer page,
        HttpServletRequest request,
        Model model) {
        if (optionService.getArchivesPrefix().equals(prefix)) {
            return postModel.archives(page, model);
        }

        if (optionService.getJournalsPrefix().equals(prefix)) {
            return journalModel.list(page, model);
        }

        if (optionService.getPhotosPrefix().equals(prefix)) {
            return photoModel.list(page, model);
        }

        throw buildPathNotFoundException();
    }

    @GetMapping("{prefix}/{slug}")
    public String content(@PathVariable("prefix") String prefix,
        @PathVariable("slug") String slug,
        @RequestParam(value = "token", required = false) String token,
        Model model) {
        PostPermalinkType postPermalinkType = optionService.getPostPermalinkType();
        if (optionService.getArchivesPrefix().equals(prefix)) {
            if (postPermalinkType.equals(PostPermalinkType.DEFAULT)) {
                Post post = postService.getBySlug(slug);
                return postModel.content(post, token, model);
            }
            if (postPermalinkType.equals(PostPermalinkType.ID_SLUG)
                && StringUtils.isNumeric(slug)) {
                Post post = postService.getById(Integer.parseInt(slug));
                return postModel.content(post, token, model);
            }
        }

        if (optionService.getCategoriesPrefix().equals(prefix)) {
            return categoryModel.listPost(model, slug, 1);
        }

        if (optionService.getTagsPrefix().equals(prefix)) {
            return tagModel.listPost(model, slug, 1);
        }

        if (postPermalinkType.equals(PostPermalinkType.YEAR) && prefix.length() == 4
            && StringUtils.isNumeric(prefix)) {
            Post post = postService.getBy(Integer.parseInt(prefix), slug);
            return postModel.content(post, token, model);
        }

        if (optionService.getSheetPermalinkType().equals(SheetPermalinkType.SECONDARY)
            && optionService.getSheetPrefix().equals(prefix)) {
            Sheet sheet = sheetService.getBySlug(slug);
            return sheetModel.content(sheet, token, model);
        }

        throw buildPathNotFoundException();
    }

    @GetMapping("{prefix}/{slug}/page/{page:\\d+}")
    public String content(@PathVariable("prefix") String prefix,
        @PathVariable("slug") String slug,
        @PathVariable("page") Integer page,
        Model model) {
        if (optionService.getCategoriesPrefix().equals(prefix)) {
            return categoryModel.listPost(model, slug, page);
        }

        if (optionService.getTagsPrefix().equals(prefix)) {
            return tagModel.listPost(model, slug, page);
        }

        throw buildPathNotFoundException();
    }

    @GetMapping("{year:\\d+}/{month:\\d+}/{slug}")
    public String content(@PathVariable("year") Integer year,
        @PathVariable("month") Integer month,
        @PathVariable("slug") String slug,
        @RequestParam(value = "token", required = false) String token,
        Model model) {
        PostPermalinkType postPermalinkType = optionService.getPostPermalinkType();
        if (postPermalinkType.equals(PostPermalinkType.DATE)) {
            Post post = postService.getBy(year, month, slug);
            return postModel.content(post, token, model);
        }

        throw buildPathNotFoundException();
    }

    @GetMapping("{year:\\d+}/{month:\\d+}/{day:\\d+}/{slug}")
    public String content(@PathVariable("year") Integer year,
        @PathVariable("month") Integer month,
        @PathVariable("day") Integer day,
        @PathVariable("slug") String slug,
        @RequestParam(value = "token", required = false) String token,
        Model model) {
        PostPermalinkType postPermalinkType = optionService.getPostPermalinkType();
        if (postPermalinkType.equals(PostPermalinkType.DAY)) {
            Post post = postService.getBy(year, month, day, slug);
            return postModel.content(post, token, model);
        }

        throw buildPathNotFoundException();
    }

    @PostMapping(value = "content/{type}/{slug:.*}/authentication")
    @CacheLock(traceRequest = true, expired = 2)
    public String password(@PathVariable("type") String type,
        @PathVariable("slug") String slug,
        @RequestParam(value = "password") String password,
        HttpServletRequest request) throws UnsupportedEncodingException {
        if (EncryptTypeEnum.POST.getName().equals(type)) {
            return authenticatePost(slug, type, password, request);
        } else if (EncryptTypeEnum.CATEGORY.getName().equals(type)) {
            return authenticateCategory(slug, type, password, request);
        } else {
            throw new UnsupportedException("未知的加密类型");
        }
    }

    private String authenticatePost(String slug, String type, String password,
        HttpServletRequest request) {
        ContentAuthenticationRequest authRequest = new ContentAuthenticationRequest();
        authRequest.setPassword(password);
        Post post = postService.getBy(PostStatus.INTIMATE, slug);
        post.setSlug(URLEncoder.encode(post.getSlug(), StandardCharsets.UTF_8));

        authRequest.setId(post.getId());
        authRequest.setPrincipal(EncryptTypeEnum.POST.getName());
        try {
            providerManager.authenticate(authRequest);
            BasePostMinimalDTO basePostMinimal = postRenderAssembler.convertToMinimal(post);
            return "redirect:" + buildRedirectUrl(basePostMinimal.getFullPath());
        } catch (AuthenticationException e) {
            request.setAttribute("errorMsg", e.getMessage());
            request.setAttribute("type", type);
            request.setAttribute("slug", slug);
            return getPasswordPageUriToForward();
        }
    }

    private String authenticateCategory(String slug, String type, String password,
        HttpServletRequest request) {
        ContentAuthenticationRequest authRequest = new ContentAuthenticationRequest();
        authRequest.setPassword(password);
        Category category = categoryService.getBySlugOfNonNull(slug);
        category.setSlug(URLEncoder.encode(category.getSlug(), StandardCharsets.UTF_8));

        authRequest.setId(category.getId());
        authRequest.setPrincipal(EncryptTypeEnum.CATEGORY.getName());
        try {
            providerManager.authenticate(authRequest);
            CategoryDTO categoryDto = categoryService.convertTo(category);
            return "redirect:" + buildRedirectUrl(categoryDto.getFullPath());
        } catch (AuthenticationException e) {
            request.setAttribute("errorMsg", e.getMessage());
            request.setAttribute("type", type);
            request.setAttribute("slug", slug);
            return getPasswordPageUriToForward();
        }
    }

    private String getPasswordPageUriToForward() {
        if (themeService.templateExists(POST_PASSWORD_TEMPLATE + SUFFIX_FTL)) {
            return themeService.render(POST_PASSWORD_TEMPLATE);
        }
        return "common/template/" + POST_PASSWORD_TEMPLATE;
    }

    private NotFoundException buildPathNotFoundException() {
        var requestAttributes = RequestContextHolder.currentRequestAttributes();

        var requestUri = "";
        if (requestAttributes instanceof ServletRequestAttributes) {
            requestUri =
                ((ServletRequestAttributes) requestAttributes).getRequest().getRequestURI();
        }
        return new NotFoundException("无法定位到该路径：" + requestUri);
    }

    private String buildRedirectUrl(String fullPath) {
        StringBuilder redirectUrl = new StringBuilder();

        if (!optionService.isEnabledAbsolutePath()) {
            redirectUrl.append(optionService.getBlogBaseUrl());
        }
        redirectUrl.append(fullPath);
        return redirectUrl.toString();
    }
}
