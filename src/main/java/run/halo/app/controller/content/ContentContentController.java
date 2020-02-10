package run.halo.app.controller.content;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import run.halo.app.controller.content.model.CategoryModel;
import run.halo.app.controller.content.model.PostModel;
import run.halo.app.controller.content.model.SheetModel;
import run.halo.app.controller.content.model.TagModel;
import run.halo.app.exception.NotFoundException;
import run.halo.app.model.entity.Post;
import run.halo.app.model.entity.Sheet;
import run.halo.app.model.enums.PostPermalinkType;
import run.halo.app.model.properties.PermalinkProperties;
import run.halo.app.service.OptionService;
import run.halo.app.service.PostService;
import run.halo.app.service.SheetService;

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

    private final OptionService optionService;

    private final PostService postService;

    private final SheetService sheetService;

    public ContentContentController(PostModel postModel,
                                    SheetModel sheetModel,
                                    CategoryModel categoryModel,
                                    TagModel tagModel,
                                    OptionService optionService,
                                    PostService postService,
                                    SheetService sheetService) {
        this.postModel = postModel;
        this.sheetModel = sheetModel;
        this.categoryModel = categoryModel;
        this.tagModel = tagModel;
        this.optionService = optionService;
        this.postService = postService;
        this.sheetService = sheetService;
    }

    @GetMapping("{prefix}")
    public String content(@PathVariable("prefix") String prefix,
                          Model model) {
        String archivesPrefix = optionService.getByPropertyOrDefault(PermalinkProperties.ARCHIVES_PREFIX, String.class, PermalinkProperties.ARCHIVES_PREFIX.defaultValue());
        String categoriesPrefix = optionService.getByPropertyOrDefault(PermalinkProperties.CATEGORIES_PREFIX, String.class, PermalinkProperties.CATEGORIES_PREFIX.defaultValue());
        String tagsPrefix = optionService.getByPropertyOrDefault(PermalinkProperties.TAGS_PREFIX, String.class, PermalinkProperties.TAGS_PREFIX.defaultValue());

        if (archivesPrefix.equals(prefix)) {
            return postModel.list(1, model, "is_archives", "archives");
        } else if (categoriesPrefix.equals(prefix)) {
            return categoryModel.list(model);
        } else if (tagsPrefix.equals(prefix)) {
            return tagModel.list(model);
        } else {
            throw new NotFoundException("Not Found");
        }
    }

    @GetMapping("{prefix}/page/{page:\\d+}")
    public String content(@PathVariable("prefix") String prefix,
                          @PathVariable(value = "page") Integer page,
                          Model model) {
        String archivesPrefix = optionService.getByPropertyOrDefault(PermalinkProperties.ARCHIVES_PREFIX, String.class, PermalinkProperties.ARCHIVES_PREFIX.defaultValue());
        if (archivesPrefix.equals(prefix)) {
            return postModel.list(page, model, "is_archives", "archives");
        } else {
            throw new NotFoundException("Not Found");
        }
    }

    @GetMapping("{prefix}/{url:.+}")
    public String content(@PathVariable("prefix") String prefix,
                          @PathVariable("url") String url,
                          @RequestParam(value = "token", required = false) String token,
                          Model model) {
        PostPermalinkType postPermalinkType = optionService.getPostPermalinkType();
        String archivesPrefix = optionService.getByPropertyOrDefault(PermalinkProperties.ARCHIVES_PREFIX, String.class, PermalinkProperties.ARCHIVES_PREFIX.defaultValue());
        String sheetPrefix = optionService.getByPropertyOrDefault(PermalinkProperties.SHEET_PREFIX, String.class, PermalinkProperties.SHEET_PREFIX.defaultValue());
        String categoriesPrefix = optionService.getByPropertyOrDefault(PermalinkProperties.CATEGORIES_PREFIX, String.class, PermalinkProperties.CATEGORIES_PREFIX.defaultValue());
        String tagsPrefix = optionService.getByPropertyOrDefault(PermalinkProperties.TAGS_PREFIX, String.class, PermalinkProperties.TAGS_PREFIX.defaultValue());

        if (postPermalinkType.equals(PostPermalinkType.DEFAULT) && archivesPrefix.equals(prefix)) {
            Post post = postService.getByUrl(url);
            return postModel.content(post, token, model);
        } else if (sheetPrefix.equals(prefix)) {
            Sheet sheet = sheetService.getByUrl(url);
            return sheetModel.content(sheet, token, model);
        } else if (categoriesPrefix.equals(prefix)) {
            return categoryModel.listPost(model, url, 1);
        } else if (tagsPrefix.equals(prefix)) {
            return tagModel.listPost(model, url, 1);
        } else {
            throw new NotFoundException("Not Found");
        }
    }

    @GetMapping("{prefix}/{url}/page/{page:\\d+}")
    public String content(@PathVariable("prefix") String prefix,
                          @PathVariable("url") String url,
                          @PathVariable("page") Integer page,
                          Model model) {
        String categoriesPrefix = optionService.getByPropertyOrDefault(PermalinkProperties.CATEGORIES_PREFIX, String.class, PermalinkProperties.CATEGORIES_PREFIX.defaultValue());
        String tagsPrefix = optionService.getByPropertyOrDefault(PermalinkProperties.TAGS_PREFIX, String.class, PermalinkProperties.TAGS_PREFIX.defaultValue());

        if (categoriesPrefix.equals(prefix)) {
            return categoryModel.listPost(model, url, page);
        } else if (tagsPrefix.equals(prefix)) {
            return tagModel.listPost(model, url, page);
        } else {
            throw new NotFoundException("Not Found");
        }
    }

    @GetMapping("{year:\\d+}/{month:\\d+}/{url:.+}")
    public String content(@PathVariable("year") Integer year,
                          @PathVariable("month") Integer month,
                          @PathVariable("url") String url,
                          @RequestParam(value = "token", required = false) String token,
                          Model model) {
        PostPermalinkType postPermalinkType = optionService.getPostPermalinkType();
        if (postPermalinkType.equals(PostPermalinkType.DATE)) {
            Post post = postService.getBy(year, month, url);
            return postModel.content(post, token, model);
        } else {
            throw new NotFoundException("Not Found");
        }
    }

    @GetMapping("{year:\\d+}/{month:\\d+}/{day:\\d+}/{url:.+}")
    public String content(@PathVariable("year") Integer year,
                          @PathVariable("month") Integer month,
                          @PathVariable("day") Integer day,
                          @PathVariable("url") String url,
                          @RequestParam(value = "token", required = false) String token,
                          Model model) {
        PostPermalinkType postPermalinkType = optionService.getPostPermalinkType();
        if (postPermalinkType.equals(PostPermalinkType.DAY)) {
            Post post = postService.getBy(year, month, day, url);
            return postModel.content(post, token, model);
        } else {
            throw new NotFoundException("Not Found");
        }
    }
}
