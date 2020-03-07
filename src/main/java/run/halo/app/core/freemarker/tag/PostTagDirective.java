package run.halo.app.core.freemarker.tag;

import cn.hutool.core.util.PageUtil;
import freemarker.core.Environment;
import freemarker.template.*;
import org.springframework.stereotype.Component;
import run.halo.app.model.entity.Post;
import run.halo.app.model.enums.PostStatus;
import run.halo.app.model.support.HaloConst;
import run.halo.app.model.support.Pagination;
import run.halo.app.model.support.RainbowPage;
import run.halo.app.service.OptionService;
import run.halo.app.service.PostCategoryService;
import run.halo.app.service.PostService;
import run.halo.app.service.PostTagService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Freemarker custom tag of post.
 *
 * @author ryanwang
 * @date 2018-04-26
 */
@Component
public class PostTagDirective implements TemplateDirectiveModel {

    private final PostService postService;

    private final OptionService optionService;

    private final PostTagService postTagService;

    private final PostCategoryService postCategoryService;

    public PostTagDirective(Configuration configuration,
                            PostService postService,
                            OptionService optionService,
                            PostTagService postTagService,
                            PostCategoryService postCategoryService) {
        this.postService = postService;
        this.optionService = optionService;
        this.postTagService = postTagService;
        this.postCategoryService = postCategoryService;
        configuration.setSharedVariable("postTag", this);
    }

    @Override
    public void execute(Environment env, Map params, TemplateModel[] loopVars, TemplateDirectiveBody body) throws TemplateException, IOException {
        final DefaultObjectWrapperBuilder builder = new DefaultObjectWrapperBuilder(Configuration.VERSION_2_3_25);
        if (params.containsKey(HaloConst.METHOD_KEY)) {
            String method = params.get(HaloConst.METHOD_KEY).toString();
            switch (method) {
                case "latest":
                    int top = Integer.parseInt(params.get("top").toString());
                    List<Post> posts = postService.listLatest(top);
                    env.setVariable("posts", builder.build().wrap(postService.convertToListVo(posts)));
                    break;
                case "count":
                    env.setVariable("count", builder.build().wrap(postService.countByStatus(PostStatus.PUBLISHED)));
                    break;
                case "archiveYear":
                    env.setVariable("archives", builder.build().wrap(postService.listYearArchives()));
                    break;
                case "archiveMonth":
                    env.setVariable("archives", builder.build().wrap(postService.listMonthArchives()));
                    break;
                case "archive":
                    String type = params.get("type").toString();
                    env.setVariable("archives", builder.build().wrap("year".equals(type) ? postService.listYearArchives() : postService.listMonthArchives()));
                    break;
                case "listByCategoryId":
                    Integer categoryId = Integer.parseInt(params.get("categoryId").toString());
                    env.setVariable("posts", builder.build().wrap(postService.convertToListVo(postCategoryService.listPostBy(categoryId, PostStatus.PUBLISHED))));
                    break;
                case "listByCategorySlug":
                    String categorySlug = params.get("categorySlug").toString();
                    env.setVariable("posts", builder.build().wrap(postService.convertToListVo(postCategoryService.listPostBy(categorySlug, PostStatus.PUBLISHED))));
                    break;
                case "listByTagId":
                    Integer tagId = Integer.parseInt(params.get("tagId").toString());
                    env.setVariable("posts", builder.build().wrap(postService.convertToListVo(postTagService.listPostsBy(tagId, PostStatus.PUBLISHED))));
                    break;
                case "listByTagSlug":
                    String tagSlug = params.get("tagSlug").toString();
                    env.setVariable("posts", builder.build().wrap(postService.convertToListVo(postTagService.listPostsBy(tagSlug, PostStatus.PUBLISHED))));
                    break;
                case "pagination":

                    // Get params
                    int page = Integer.parseInt(params.get("page").toString());
                    int total = Integer.parseInt(params.get("total").toString());
                    int display = Integer.parseInt(params.get("display").toString());
                    boolean isArchives = Boolean.parseBoolean(params.get("isArchives").toString());

                    // Create pagination object.
                    Pagination pagination = new Pagination();

                    // Generate next page full path and pre page full path.
                    StringBuilder nextPageFullPath = new StringBuilder();
                    StringBuilder prePageFullPath = new StringBuilder();

                    if (optionService.isEnabledAbsolutePath()) {
                        nextPageFullPath.append(optionService.getBlogBaseUrl());
                        prePageFullPath.append(optionService.getBlogBaseUrl());
                    }

                    if (isArchives) {
                        nextPageFullPath.append("/")
                            .append(optionService.getArchivesPrefix());
                        prePageFullPath.append("/")
                            .append(optionService.getArchivesPrefix());
                    }

                    nextPageFullPath.append("/page/")
                        .append(page + 2)
                        .append(optionService.getPathSuffix());

                    if (page == 1) {
                        prePageFullPath.append("/");
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

                    if (isArchives) {
                        fullPath.append("/")
                            .append(optionService.getArchivesPrefix());
                    }

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
        body.render(env.getOut());
    }

}
