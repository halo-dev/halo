package cc.ryanc.halo.web.controller.admin;

import cc.ryanc.halo.model.support.Theme;
import cc.ryanc.halo.service.OptionService;
import cc.ryanc.halo.utils.HaloUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

/**
 * Themes controller
 *
 * @author : RYAN0UP
 * @date : 2019-03-14
 */
@Controller
@RequestMapping(value = "/admin/themes")
public class ThemeController {

    private OptionService optionService;

    public ThemeController(OptionService optionService) {
        this.optionService = optionService;
    }

    @GetMapping
    public String themes(Model model) {
        Map<String, String> options = optionService.listOptions();
        model.addAttribute("options",options);

        List<Theme> themes = HaloUtils.getThemes();

        model.addAttribute("themes", themes);

        return "admin/admin_theme";
    }
}
