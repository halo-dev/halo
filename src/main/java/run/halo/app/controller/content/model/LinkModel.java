package run.halo.app.controller.content.model;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import run.halo.app.service.LinkService;
import run.halo.app.service.OptionService;
import run.halo.app.service.ThemeService;

import static org.springframework.data.domain.Sort.Direction.ASC;

/**
 * @author ryanwang
 * @date 2020-03-04
 */
@Component
public class LinkModel {

    private final ThemeService themeService;

    private final OptionService optionService;
    private LinkService linkService;

    public LinkModel(ThemeService themeService,
        OptionService optionService, LinkService linkService) {
        this.themeService = themeService;
        this.optionService = optionService;
        this.linkService = linkService;
    }

    public String list(Model model) {
        // links
        model.addAttribute("links", linkService.listDtos(Sort.by(ASC, "priority")));
        model.addAttribute("is_links", true);
        model.addAttribute("meta_keywords", optionService.getSeoKeywords());
        model.addAttribute("meta_description", optionService.getSeoDescription());
        return themeService.render("links");
    }
}
