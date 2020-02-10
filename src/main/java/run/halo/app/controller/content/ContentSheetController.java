package run.halo.app.controller.content;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import run.halo.app.model.dto.PhotoDTO;
import run.halo.app.service.PhotoService;
import run.halo.app.service.ThemeService;

import static org.springframework.data.domain.Sort.Direction.DESC;

/**
 * Content sheet controller.
 *
 * @author ryanwang
 * @author evanwang
 * @date 2019-03-21
 */
@Controller
public class ContentSheetController {


    private final ThemeService themeService;

    private final PhotoService photoService;

    public ContentSheetController(ThemeService themeService,
                                  PhotoService photoService) {
        this.themeService = themeService;
        this.photoService = photoService;
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
}
