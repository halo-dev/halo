package run.halo.app.controller.content;

import cn.hutool.core.util.IdUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import run.halo.app.cache.AbstractStringCacheStore;
import run.halo.app.cache.lock.CacheLock;
import run.halo.app.controller.content.model.*;
import run.halo.app.exception.NotFoundException;
import run.halo.app.model.dto.post.BasePostMinimalDTO;
import run.halo.app.model.entity.Post;
import run.halo.app.model.entity.Sheet;
import run.halo.app.model.enums.PostPermalinkType;
import run.halo.app.model.enums.PostStatus;
import run.halo.app.service.OptionService;
import run.halo.app.service.PostService;
import run.halo.app.service.SheetService;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * @author ryanwang
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

    private final AbstractStringCacheStore cacheStore;

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
            AbstractStringCacheStore cacheStore) {
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
        this.cacheStore = cacheStore;
    }

    @GetMapping("{prefix}")
    public String content(@PathVariable("prefix") String prefix,
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
        return null;
    }

    @GetMapping("{prefix}/page/{page:\\d+}")
    public String content(@PathVariable("prefix") String prefix,
            @PathVariable(value = "page") Integer page,
            Model model) {
        if (optionService.getArchivesPrefix().equals(prefix)) {
            return postModel.archives(page, model);
        } else if (optionService.getJournalsPrefix().equals(prefix)) {
            return journalModel.list(page, model);
        } else if (optionService.getPhotosPrefix().equals(prefix)) {
            return photoModel.list(page, model);
        } else {
            throw new NotFoundException("Not Found");
        }
    }

    @GetMapping("{prefix}/{slug}")
    public String content(@PathVariable("prefix") String prefix,
            @PathVariable("slug") String slug,
            @RequestParam(value = "token", required = false) String token,
            Model model) {
        PostPermalinkType postPermalinkType = optionService.getPostPermalinkType();

        if (postPermalinkType.equals(PostPermalinkType.DEFAULT) && optionService.getArchivesPrefix().equals(prefix)) {
            Post post = postService.getBySlug(slug);
            return postModel.content(post, token, model);
        } else if (postPermalinkType.equals(PostPermalinkType.YEAR) && prefix.length() == 4 && StringUtils.isNumeric(prefix)) {
            Post post = postService.getBy(Integer.parseInt(prefix), slug);
            return postModel.content(post, token, model);
        } else if (optionService.getSheetPrefix().equals(prefix)) {
            Sheet sheet = sheetService.getBySlug(slug);
            return sheetModel.content(sheet, token, model);
        } else if (optionService.getCategoriesPrefix().equals(prefix)) {
            return categoryModel.listPost(model, slug, 1);
        } else if (optionService.getTagsPrefix().equals(prefix)) {
            return tagModel.listPost(model, slug, 1);
        } else {
            throw new NotFoundException("Not Found");
        }
    }

    @GetMapping("{prefix}/{slug}/page/{page:\\d+}")
    public String content(@PathVariable("prefix") String prefix,
            @PathVariable("slug") String slug,
            @PathVariable("page") Integer page,
            Model model) {
        if (optionService.getCategoriesPrefix().equals(prefix)) {
            return categoryModel.listPost(model, slug, page);
        } else if (optionService.getTagsPrefix().equals(prefix)) {
            return tagModel.listPost(model, slug, page);
        } else {
            throw new NotFoundException("Not Found");
        }
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
        } else {
            throw new NotFoundException("Not Found");
        }
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
        } else {
            throw new NotFoundException("Not Found");
        }
    }

    @PostMapping(value = "archives/{slug:.*}/password")
    @CacheLock(traceRequest = true, expired = 2)
    public String password(@PathVariable("slug") String slug,
            @RequestParam(value = "password") String password) throws UnsupportedEncodingException {
        Post post = postService.getBy(PostStatus.INTIMATE, slug);

        post.setSlug(URLEncoder.encode(post.getSlug(), StandardCharsets.UTF_8.name()));

        BasePostMinimalDTO postMinimalDTO = postService.convertToMinimal(post);

        StringBuilder redirectUrl = new StringBuilder();

        if (!optionService.isEnabledAbsolutePath()) {
            redirectUrl.append(optionService.getBlogBaseUrl());
        }

        redirectUrl.append(postMinimalDTO.getFullPath());

        if (password.equals(post.getPassword())) {
            String token = IdUtil.simpleUUID();
            cacheStore.putAny(token, token, 10, TimeUnit.SECONDS);

            if (optionService.getPostPermalinkType().equals(PostPermalinkType.ID)) {
                redirectUrl.append("&token=")
                        .append(token);
            } else {
                redirectUrl.append("?token=")
                        .append(token);
            }
        }

        return "redirect:" + redirectUrl;
    }
}
