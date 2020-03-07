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
 * @date 2020-03-07
 */
@Component
public class PaginationTagDirective implements TemplateDirectiveModel {

    private final OptionService optionService;

    public PaginationTagDirective(Configuration configuration,
                                  OptionService optionService) {
        this.optionService = optionService;
        configuration.setSharedVariable("paginationTag", this);
    }

    @Override
    public void execute(Environment env, Map params, TemplateModel[] loopVars, TemplateDirectiveBody body) throws TemplateException, IOException {
        final DefaultObjectWrapperBuilder builder = new DefaultObjectWrapperBuilder(Configuration.VERSION_2_3_25);
        if (params.containsKey(HaloConst.METHOD_KEY)) {

            // Get params
            String method = params.get(HaloConst.METHOD_KEY).toString();
            int page = Integer.parseInt(params.get("page").toString());
            int total = Integer.parseInt(params.get("total").toString());
            int display = Integer.parseInt(params.get("display").toString());

            // Create pagination object.
            Pagination pagination = new Pagination();

            // Generate next page full path and pre page full path.
            StringBuilder nextPageFullPath = new StringBuilder();
            StringBuilder prevPageFullPath = new StringBuilder();

            if (optionService.isEnabledAbsolutePath()) {
                nextPageFullPath.append(optionService.getBlogBaseUrl());
                prevPageFullPath.append(optionService.getBlogBaseUrl());
            }

            int[] rainbow = PageUtil.rainbow(page + 1, total, display);

            List<RainbowPage> rainbowPages = new ArrayList<>();
            StringBuilder fullPath = new StringBuilder();

            if (optionService.isEnabledAbsolutePath()) {
                fullPath.append(optionService.getBlogBaseUrl());
            }

            String pathSuffix = optionService.getPathSuffix();

            switch (method) {
                case "index":

                    nextPageFullPath.append("/page/")
                        .append(page + 2)
                        .append(pathSuffix);

                    if (page == 1) {
                        prevPageFullPath.append("/");
                    } else {
                        prevPageFullPath.append("/page/")
                            .append(page)
                            .append(pathSuffix);
                    }

                    fullPath.append("/page/");

                    for (int current : rainbow) {
                        RainbowPage rainbowPage = new RainbowPage();
                        rainbowPage.setPage(current);
                        rainbowPage.setFullPath(fullPath.toString() + current + pathSuffix);
                        rainbowPage.setIsCurrent(page + 1 == current);
                        rainbowPages.add(rainbowPage);
                    }
                    break;
                case "archives":

                    nextPageFullPath.append("/")
                        .append(optionService.getArchivesPrefix());
                    prevPageFullPath.append("/")
                        .append(optionService.getArchivesPrefix());

                    nextPageFullPath.append("/page/")
                        .append(page + 2)
                        .append(pathSuffix);

                    if (page == 1) {
                        prevPageFullPath.
                            append(pathSuffix);
                    } else {
                        prevPageFullPath.append("/page/")
                            .append(page)
                            .append(pathSuffix);
                    }

                    fullPath.append("/")
                        .append(optionService.getArchivesPrefix());

                    fullPath.append("/page/");

                    for (int current : rainbow) {
                        RainbowPage rainbowPage = new RainbowPage();
                        rainbowPage.setPage(current);
                        rainbowPage.setFullPath(fullPath.toString() + current + pathSuffix);
                        rainbowPage.setIsCurrent(page + 1 == current);
                        rainbowPages.add(rainbowPage);
                    }
                    break;
                case "search":
                    String keyword = params.get("keyword").toString();

                    nextPageFullPath.append("/")
                        .append("search");
                    prevPageFullPath.append("/")
                        .append("search");

                    nextPageFullPath.append("/page/")
                        .append(page + 2)
                        .append(pathSuffix)
                        .append("?keyword=")
                        .append(keyword);

                    if (page == 1) {
                        prevPageFullPath.append(pathSuffix)
                            .append("?keyword=")
                            .append(keyword);
                    } else {
                        prevPageFullPath.append("/page/")
                            .append(page)
                            .append(pathSuffix)
                            .append("?keyword=")
                            .append(keyword);
                    }

                    fullPath.append("/")
                        .append("search");

                    fullPath.append("/page/");

                    for (int current : rainbow) {
                        RainbowPage rainbowPage = new RainbowPage();
                        rainbowPage.setPage(current);
                        rainbowPage.setFullPath(fullPath.toString() + current + pathSuffix + "?keyword=" + keyword);
                        rainbowPage.setIsCurrent(page + 1 == current);
                        rainbowPages.add(rainbowPage);
                    }
                    break;
                case "tagPosts":
                    String tagSlug = params.get("slug").toString();

                    nextPageFullPath.append("/")
                        .append(optionService.getTagsPrefix())
                        .append("/")
                        .append(tagSlug);
                    prevPageFullPath.append("/")
                        .append(optionService.getTagsPrefix())
                        .append("/")
                        .append(tagSlug);

                    nextPageFullPath.append("/page/")
                        .append(page + 2)
                        .append(pathSuffix);

                    if (page == 1) {
                        prevPageFullPath.append(pathSuffix);
                    } else {
                        prevPageFullPath.append("/page/")
                            .append(page)
                            .append(pathSuffix);
                    }

                    fullPath.append("/")
                        .append(optionService.getTagsPrefix())
                        .append("/")
                        .append(tagSlug);

                    fullPath.append("/page/");

                    for (int current : rainbow) {
                        RainbowPage rainbowPage = new RainbowPage();
                        rainbowPage.setPage(current);
                        rainbowPage.setFullPath(fullPath.toString() + current + pathSuffix);
                        rainbowPage.setIsCurrent(page + 1 == current);
                        rainbowPages.add(rainbowPage);
                    }
                    break;
                case "categoryPosts":
                    String categorySlug = params.get("slug").toString();

                    nextPageFullPath.append("/")
                        .append(optionService.getCategoriesPrefix())
                        .append("/")
                        .append(categorySlug);
                    prevPageFullPath.append("/")
                        .append(optionService.getCategoriesPrefix())
                        .append("/")
                        .append(categorySlug);

                    nextPageFullPath.append("/page/")
                        .append(page + 2)
                        .append(pathSuffix);

                    if (page == 1) {
                        prevPageFullPath.append(pathSuffix);
                    } else {
                        prevPageFullPath.append("/page/")
                            .append(page)
                            .append(pathSuffix);
                    }

                    fullPath.append("/")
                        .append(optionService.getCategoriesPrefix())
                        .append("/")
                        .append(categorySlug);

                    fullPath.append("/page/");

                    for (int current : rainbow) {
                        RainbowPage rainbowPage = new RainbowPage();
                        rainbowPage.setPage(current);
                        rainbowPage.setFullPath(fullPath.toString() + current + pathSuffix);
                        rainbowPage.setIsCurrent(page + 1 == current);
                        rainbowPages.add(rainbowPage);
                    }
                    break;
                case "photos":

                    nextPageFullPath.append("/")
                        .append(optionService.getPhotosPrefix());
                    prevPageFullPath.append("/")
                        .append(optionService.getPhotosPrefix());

                    nextPageFullPath.append("/page/")
                        .append(page + 2)
                        .append(pathSuffix);

                    if (page == 1) {
                        prevPageFullPath.append(pathSuffix);
                    } else {
                        prevPageFullPath.append("/page/")
                            .append(page)
                            .append(pathSuffix);
                    }

                    fullPath.append("/")
                        .append(optionService.getPhotosPrefix());

                    fullPath.append("/page/");

                    for (int current : rainbow) {
                        RainbowPage rainbowPage = new RainbowPage();
                        rainbowPage.setPage(current);
                        rainbowPage.setFullPath(fullPath.toString() + current + pathSuffix);
                        rainbowPage.setIsCurrent(page + 1 == current);
                        rainbowPages.add(rainbowPage);
                    }
                    break;
                case "journals":

                    nextPageFullPath.append("/")
                        .append(optionService.getJournalsPrefix());
                    prevPageFullPath.append("/")
                        .append(optionService.getJournalsPrefix());

                    nextPageFullPath.append("/page/")
                        .append(page + 2)
                        .append(pathSuffix);

                    if (page == 1) {
                        prevPageFullPath.append(pathSuffix);
                    } else {
                        prevPageFullPath.append("/page/")
                            .append(page)
                            .append(pathSuffix);
                    }

                    fullPath.append("/")
                        .append(optionService.getJournalsPrefix());

                    fullPath.append("/page/");

                    for (int current : rainbow) {
                        RainbowPage rainbowPage = new RainbowPage();
                        rainbowPage.setPage(current);
                        rainbowPage.setFullPath(fullPath.toString() + current + pathSuffix);
                        rainbowPage.setIsCurrent(page + 1 == current);
                        rainbowPages.add(rainbowPage);
                    }
                    break;
                default:
                    break;
            }
            pagination.setNextPageFullPath(nextPageFullPath.toString());
            pagination.setPrevPageFullPath(prevPageFullPath.toString());
            pagination.setRainbowPages(rainbowPages);
            pagination.setHasNext(total != page + 1);
            pagination.setHasPrev(page != 0);
            env.setVariable("pagination", builder.build().wrap(pagination));
        }
        body.render(env.getOut());
    }
}
