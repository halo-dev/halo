package run.halo.app.core.freemarker.tag;

import cn.hutool.core.util.PageUtil;
import freemarker.core.Environment;
import freemarker.template.*;
import org.springframework.stereotype.Component;
import run.halo.app.model.support.HaloConst;
import run.halo.app.model.support.Pagination;
import run.halo.app.model.support.RainbowPage;
import run.halo.app.service.OptionService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author ryanwang
 * @date 2020-03-06
 */
@Component
public class JournalTagDirective implements TemplateDirectiveModel {

    private final OptionService optionService;

    public JournalTagDirective(Configuration configuration,
                               OptionService optionService) {
        this.optionService = optionService;
        configuration.setSharedVariable("journalTag", this);
    }

    @Override
    public void execute(Environment env, Map params, TemplateModel[] loopVars, TemplateDirectiveBody body) throws TemplateException, IOException {
        final DefaultObjectWrapperBuilder builder = new DefaultObjectWrapperBuilder(Configuration.VERSION_2_3_25);

        if (params.containsKey(HaloConst.METHOD_KEY)) {
            String method = params.get(HaloConst.METHOD_KEY).toString();
            switch (method) {
                case "pagination":
                    // Get params
                    int page = Integer.parseInt(params.get("page").toString());
                    int total = Integer.parseInt(params.get("total").toString());
                    int display = Integer.parseInt(params.get("display").toString());
                    String slug = params.get("slug").toString();

                    // Create pagination object.
                    Pagination pagination = new Pagination();

                    // Generate next page full path and pre page full path.
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
                        .append(page + 2)
                        .append(optionService.getPathSuffix());

                    if (page == 1) {
                        prePageFullPath.append(optionService.getPathSuffix());
                    } else {
                        prePageFullPath.append("/page/")
                            .append(page)
                            .append(optionService.getPathSuffix());
                    }

                    pagination.setNextPageFullPath(nextPageFullPath.toString());
                    pagination.setPrePageFullPath(prePageFullPath.toString());

                    // Generate rainbow page number.
                    int[] rainbow = PageUtil.rainbow(page + 1, total, display);

                    List<RainbowPage> rainbowPages = new ArrayList<>();
                    StringBuilder fullPath = new StringBuilder();
                    if (optionService.isEnabledAbsolutePath()) {
                        fullPath.append(optionService.getBlogBaseUrl());
                    }

                    nextPageFullPath.append("/")
                        .append(optionService.getJournalsPrefix());

                    fullPath.append("/page/");

                    for (int current : rainbow) {
                        RainbowPage rainbowPage = new RainbowPage();
                        rainbowPage.setPage(current);
                        rainbowPage.setFullPath(fullPath.toString() + current + optionService.getPathSuffix());
                        rainbowPage.setIsCurrent(page + 1 == current);
                        rainbowPages.add(rainbowPage);
                    }

                    pagination.setRainbowPages(rainbowPages);
                    pagination.setHasNext(total != page + 1);
                    pagination.setHasPrev(page != 0);
                    env.setVariable("pagination", builder.build().wrap(pagination));
                    break;
                default:
                    break;
            }
        }
    }
}
