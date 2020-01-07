package run.halo.app.controller.content;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import run.halo.app.controller.content.model.PostModel;
import run.halo.app.controller.content.model.SheetModel;
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

    private final OptionService optionService;

    private final PostService postService;

    private final SheetService sheetService;

    public ContentContentController(PostModel postModel,
                                    SheetModel sheetModel,
                                    OptionService optionService,
                                    PostService postService,
                                    SheetService sheetService) {
        this.postModel = postModel;
        this.sheetModel = sheetModel;
        this.optionService = optionService;
        this.postService = postService;
        this.sheetService = sheetService;
    }

    @GetMapping("{prefix}/{url}")
    public String content(@PathVariable("prefix") String prefix,
                          @PathVariable("url") String url,
                          @RequestParam(value = "token", required = false) String token,
                          Model model) {
        PostPermalinkType postPermalinkType = optionService.getPostPermalinkType();
        String archivesPrefix = optionService.getByPropertyOrDefault(PermalinkProperties.ARCHIVES_PREFIX, String.class, PermalinkProperties.ARCHIVES_PREFIX.defaultValue());
        String sheetPrefix = optionService.getByPropertyOrDefault(PermalinkProperties.SHEET_PREFIX, String.class, PermalinkProperties.SHEET_PREFIX.defaultValue());

        if (postPermalinkType.equals(PostPermalinkType.DEFAULT) && archivesPrefix.equals(prefix)) {
            Post post = postService.getByUrl(url);
            return postModel.content(post, token, model);
        } else if (sheetPrefix.equals(prefix)) {
            Sheet sheet = sheetService.getByUrl(url);
            return sheetModel.content(sheet, token, model);
        } else {
            throw new NotFoundException("Not Found");
        }
    }
}
