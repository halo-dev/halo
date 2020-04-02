package run.halo.app.controller.content.model;

import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import run.halo.app.service.OptionService;
import run.halo.app.service.ThemeService;

/**
 * @author ryanwang
 * @date 2020-03-04
 */
@Component
public class LinkModel {

    private final ThemeService themeService;

    private final OptionService optionService;

    public LinkModel(ThemeService themeService,
                     OptionService optionService) {
        this.themeService = themeService;
        this.optionService = optionService;
    }

    public String list(Model model) {
        model.addAttribute("is_links", true);
        model.addAttribute("meta_keywords", optionService.getSeoKeywords());
        model.addAttribute("meta_description", optionService.getSeoDescription());
        return themeService.render("links");
    }
}
