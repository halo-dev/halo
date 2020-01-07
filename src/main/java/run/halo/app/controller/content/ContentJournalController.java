package run.halo.app.controller.content;

import cn.hutool.core.util.PageUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import run.halo.app.model.entity.Journal;
import run.halo.app.model.enums.JournalType;
import run.halo.app.service.JournalService;
import run.halo.app.service.OptionService;
import run.halo.app.service.ThemeService;

import static org.springframework.data.domain.Sort.Direction.DESC;

/**
 * Blog journal page controller
 *
 * @author ryanwang
 * @date 2019-05-04
 */
@Slf4j
@Controller
@RequestMapping(value = "/journals")
public class ContentJournalController {

    private final JournalService journalService;

    private final OptionService optionService;

    private final ThemeService themeService;

    public ContentJournalController(JournalService journalService,
                                    OptionService optionService,
                                    ThemeService themeService) {
        this.journalService = journalService;
        this.optionService = optionService;
        this.themeService = themeService;
    }

    /**
     * Render journal page.
     *
     * @param model model
     * @return template path: themes/{theme}/journals.ftl
     */
    @GetMapping
    public String journals(Model model) {
        return this.journals(model, 1, Sort.by(DESC, "createTime"));
    }


    /**
     * Render journal page.
     *
     * @param model model
     * @param page  current page number
     * @return template path: themes/{theme}/journals.ftl
     */
    @GetMapping(value = "page/{page}")
    public String journals(Model model,
                           @PathVariable(value = "page") Integer page,
                           @SortDefault(sort = "createTime", direction = DESC) Sort sort) {
        log.debug("Requested journal page, sort info: [{}]", sort);

        int pageSize = optionService.getPostPageSize();

        Pageable pageable = PageRequest.of(page >= 1 ? page - 1 : page, pageSize, sort);

        Page<Journal> journals = journalService.pageBy(JournalType.PUBLIC, pageable);

        int[] rainbow = PageUtil.rainbow(page, journals.getTotalPages(), 3);

        model.addAttribute("is_journals", true);
        model.addAttribute("journals", journalService.convertToCmtCountDto(journals));
        model.addAttribute("rainbow", rainbow);
        return themeService.render("journals");
    }
}
