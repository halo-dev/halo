package run.halo.app.controller.content.model;

import cn.hutool.core.util.PageUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import run.halo.app.model.entity.Journal;
import run.halo.app.model.enums.JournalType;
import run.halo.app.model.properties.SheetProperties;
import run.halo.app.service.JournalService;
import run.halo.app.service.OptionService;
import run.halo.app.service.ThemeService;

import static org.springframework.data.domain.Sort.Direction.DESC;

/**
 * @author ryanwang
 * @date 2020-02-11
 */
@Component
public class JournalModel {

    private final JournalService journalService;

    private final OptionService optionService;

    private final ThemeService themeService;

    public JournalModel(JournalService journalService,
                        OptionService optionService,
                        ThemeService themeService) {
        this.journalService = journalService;
        this.optionService = optionService;
        this.themeService = themeService;
    }

    public String list(Integer page, Model model) {

        int pageSize = optionService.getByPropertyOrDefault(SheetProperties.JOURNALS_PAGE_SIZE, Integer.class, Integer.parseInt(SheetProperties.JOURNALS_PAGE_SIZE.defaultValue()));

        Pageable pageable = PageRequest.of(page >= 1 ? page - 1 : page, pageSize, Sort.by(DESC, "createTime"));

        Page<Journal> journals = journalService.pageBy(JournalType.PUBLIC, pageable);

        // TODO remove this variable
        int[] rainbow = PageUtil.rainbow(page, journals.getTotalPages(), 3);

        // Next page and previous page url.
        StringBuilder nextPageFullPath = new StringBuilder();
        StringBuilder prePageFullPath = new StringBuilder();

        if (optionService.isEnabledAbsolutePath()) {
            nextPageFullPath.append(optionService.getBlogBaseUrl())
                    .append("/");
            prePageFullPath.append(optionService.getBlogBaseUrl())
                    .append("/");
        } else {
            nextPageFullPath.append("/");
            prePageFullPath.append("/");
        }

        nextPageFullPath.append(optionService.getJournalsPrefix());
        prePageFullPath.append(optionService.getJournalsPrefix());

        nextPageFullPath.append("/page/")
                .append(journals.getNumber() + 2)
                .append(optionService.getPathSuffix());

        if (journals.getNumber() == 1) {
            prePageFullPath.append("/");
        } else {
            prePageFullPath.append("/page/")
                    .append(journals.getNumber())
                    .append(optionService.getPathSuffix());
        }

        model.addAttribute("is_journals", true);
        model.addAttribute("journals", journalService.convertToCmtCountDto(journals));
        model.addAttribute("rainbow", rainbow);
        model.addAttribute("nextPageFullPath", nextPageFullPath.toString());
        model.addAttribute("prePageFullPath", prePageFullPath.toString());
        return themeService.render("journals");
    }
}
