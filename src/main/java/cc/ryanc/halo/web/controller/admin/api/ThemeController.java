package cc.ryanc.halo.web.controller.admin.api;

import cc.ryanc.halo.model.support.Theme;
import cc.ryanc.halo.utils.ThemeUtils;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author : RYAN0UP
 * @date : 2019/3/20
 */
@RestController
@RequestMapping("/admin/api/themes")
public class ThemeController {

    /**
     * List all themes
     *
     * @return themes
     */
    @GetMapping
    @ApiOperation("List all themes")
    public List<Theme> listAll() {
        return ThemeUtils.getThemes();
    }
}
