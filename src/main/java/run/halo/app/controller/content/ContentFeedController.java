package run.halo.app.controller.content;

import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RegExUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
import run.halo.app.model.dto.CategoryDTO;
import run.halo.app.model.entity.Category;
import run.halo.app.model.entity.Post;
import run.halo.app.model.enums.PostStatus;
import run.halo.app.model.vo.PostDetailVO;
import run.halo.app.service.CategoryService;
import run.halo.app.service.OptionService;
import run.halo.app.service.PostCategoryService;
import run.halo.app.service.PostService;

import java.io.IOException;
import java.util.List;

import static org.springframework.data.domain.Sort.Direction.DESC;

/**
 * @author ryanwang
 * @date 2019-03-21
 */
@Slf4j
@Controller
public class ContentFeedController {

    private final static String UTF_8_SUFFIX = ";charset=UTF-8";

    private final static String XML_INVAID_CHAR = "[\\x00-\\x1F\\x7F]";

    private final static String XML_MEDIA_TYPE = MediaType.APPLICATION_XML_VALUE + UTF_8_SUFFIX;

    private final PostService postService;

    private final CategoryService categoryService;

    private final PostCategoryService postCategoryService;

    private final OptionService optionService;

    private final FreeMarkerConfigurer freeMarker;

    public ContentFeedController(PostService postService,
                                 CategoryService categoryService,
                                 PostCategoryService postCategoryService,
                                 OptionService optionService,
                                 FreeMarkerConfigurer freeMarker) {
        this.postService = postService;
        this.categoryService = categoryService;
        this.postCategoryService = postCategoryService;
        this.optionService = optionService;
        this.freeMarker = freeMarker;
    }

    /**
     * Get post rss
     *
     * @param model model
     * @return rss xml content
     * @throws IOException       throw IOException
     * @throws TemplateException throw TemplateException
     */
    @GetMapping(value = {"feed", "feed.xml", "rss", "rss.xml"}, produces = XML_MEDIA_TYPE)
    @ResponseBody
    public String feed(Model model) throws IOException, TemplateException {
        model.addAttribute("posts", buildPosts(buildPostPageable(optionService.getRssPageSize())));
        Template template = freeMarker.getConfiguration().getTemplate("common/web/rss.ftl");
        return FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
    }

    /**
     * Get category post rss.
     *
     * @param model model
     * @param slug  slug
     * @return rss xml content
     * @throws IOException       throw IOException
     * @throws TemplateException throw TemplateException
     */
    @GetMapping(value = {"feed/categories/{slug}", "feed/categories/{slug}.xml"}, produces = XML_MEDIA_TYPE)
    @ResponseBody
    public String feed(Model model, @PathVariable(name = "slug") String slug) throws IOException, TemplateException {
        Category category = categoryService.getBySlugOfNonNull(slug);
        CategoryDTO categoryDTO = categoryService.convertTo(category);
        model.addAttribute("category", categoryDTO);
        model.addAttribute("posts", buildCategoryPosts(buildPostPageable(optionService.getRssPageSize()), categoryDTO));
        Template template = freeMarker.getConfiguration().getTemplate("common/web/rss.ftl");
        return FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
    }

    /**
     * Get atom.xml
     *
     * @param model model
     * @return atom xml content
     * @throws IOException       IOException
     * @throws TemplateException TemplateException
     */
    @GetMapping(value = {"atom", "atom.xml"}, produces = XML_MEDIA_TYPE)
    @ResponseBody
    public String atom(Model model) throws IOException, TemplateException {
        model.addAttribute("posts", buildPosts(buildPostPageable(optionService.getRssPageSize())));
        Template template = freeMarker.getConfiguration().getTemplate("common/web/atom.ftl");
        return FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
    }

    /**
     * Get category posts atom.xml
     *
     * @param model model
     * @param slug  slug
     * @return atom xml content
     * @throws IOException       throw IOException
     * @throws TemplateException throw TemplateException
     */
    @GetMapping(value = {"atom/categories/{slug}", "atom/categories/{slug}.xml"}, produces = XML_MEDIA_TYPE)
    @ResponseBody
    public String atom(Model model, @PathVariable(name = "slug") String slug) throws IOException, TemplateException {
        Category category = categoryService.getBySlugOfNonNull(slug);
        CategoryDTO categoryDTO = categoryService.convertTo(category);
        model.addAttribute("category", categoryDTO);
        model.addAttribute("posts", buildCategoryPosts(buildPostPageable(optionService.getRssPageSize()), categoryDTO));
        Template template = freeMarker.getConfiguration().getTemplate("common/web/atom.ftl");
        return FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
    }

    /**
     * Get sitemap.xml.
     *
     * @param model model
     * @return sitemap xml content.
     * @throws IOException       IOException
     * @throws TemplateException TemplateException
     */
    @GetMapping(value = {"sitemap", "sitemap.xml"}, produces = XML_MEDIA_TYPE)
    @ResponseBody
    public String sitemapXml(Model model,
                             @PageableDefault(size = Integer.MAX_VALUE, sort = "createTime", direction = DESC) Pageable pageable) throws IOException, TemplateException {
        model.addAttribute("posts", buildPosts(pageable));
        Template template = freeMarker.getConfiguration().getTemplate("common/web/sitemap_xml.ftl");
        return FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
    }

    /**
     * Get sitemap.html.
     *
     * @param model model
     * @return template path: common/web/sitemap_html
     */
    @GetMapping(value = "sitemap.html")
    public String sitemapHtml(Model model,
                              @PageableDefault(size = Integer.MAX_VALUE, sort = "createTime", direction = DESC) Pageable pageable) {
        model.addAttribute("posts", buildPosts(pageable));
        return "common/web/sitemap_html";
    }

    /**
     * Get robots.txt
     *
     * @param model model
     * @return robots.txt content
     * @throws IOException       IOException
     * @throws TemplateException TemplateException
     */
    @GetMapping(value = "robots.txt", produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseBody
    public String robots(Model model) throws IOException, TemplateException {
        Template template = freeMarker.getConfiguration().getTemplate("common/web/robots.ftl");
        return FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
    }

    /**
     * Builds page info for post.
     *
     * @param size page size
     * @return page info
     */
    @NonNull
    private Pageable buildPostPageable(int size) {
        return PageRequest.of(0, size, Sort.by(DESC, "createTime"));
    }

    /**
     * Build posts.
     *
     * @param pageable pageable
     * @return list of post detail vo
     */
    private List<PostDetailVO> buildPosts(@NonNull Pageable pageable) {
        Assert.notNull(pageable, "Pageable must not be null");

        Page<Post> postPage = postService.pageBy(PostStatus.PUBLISHED, pageable);
        Page<PostDetailVO> posts = postService.convertToDetailVo(postPage);
        posts.getContent().forEach(postDetailVO -> {
            postDetailVO.setFormatContent(RegExUtils.replaceAll(postDetailVO.getFormatContent(), XML_INVAID_CHAR, ""));
            postDetailVO.setSummary(RegExUtils.replaceAll(postDetailVO.getSummary(), XML_INVAID_CHAR, ""));
        });
        return posts.getContent();
    }

    /**
     * Build category posts.
     *
     * @param pageable pageable must not be null.
     * @param category category
     * @return list of post detail vo.
     */
    private List<PostDetailVO> buildCategoryPosts(@NonNull Pageable pageable, @NonNull CategoryDTO category) {
        Assert.notNull(pageable, "Pageable must not be null");
        Assert.notNull(category, "Category slug must not be null");

        Page<Post> postPage = postCategoryService.pagePostBy(category.getId(), PostStatus.PUBLISHED, pageable);
        Page<PostDetailVO> posts = postService.convertToDetailVo(postPage);
        posts.getContent().forEach(postDetailVO -> {
            postDetailVO.setFormatContent(RegExUtils.replaceAll(postDetailVO.getFormatContent(), XML_INVAID_CHAR, ""));
            postDetailVO.setSummary(RegExUtils.replaceAll(postDetailVO.getSummary(), XML_INVAID_CHAR, ""));
        });
        return posts.getContent();
    }
}
