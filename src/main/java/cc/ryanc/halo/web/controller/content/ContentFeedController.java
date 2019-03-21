package cc.ryanc.halo.web.controller.content;

import cc.ryanc.halo.model.entity.Post;
import cc.ryanc.halo.model.enums.BlogProperties;
import cc.ryanc.halo.model.enums.PostStatus;
import cc.ryanc.halo.model.enums.PostType;
import cc.ryanc.halo.service.PostService;
import cn.hutool.core.util.StrUtil;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.io.IOException;
import java.util.List;

import static cc.ryanc.halo.model.support.HaloConst.OPTIONS;

/**
 * @author : RYAN0UP
 * @date : 2019-03-21
 */
@Controller
public class ContentFeedController {

    private final PostService postService;

    private final FreeMarkerConfigurer freeMarker;

    public ContentFeedController(PostService postService, FreeMarkerConfigurer freeMarker) {
        this.postService = postService;
        this.freeMarker = freeMarker;
    }

    /**
     * Get post rss
     *
     * @param model model
     * @return String
     * @throws IOException       IOException
     * @throws TemplateException TemplateException
     */
    @GetMapping(value = {"feed", "feed.xml", "rss", "rss.xml"}, produces = "application/xml;charset=UTF-8")
    @ResponseBody
    public String feed(Model model) throws IOException, TemplateException {
        String rssPosts = OPTIONS.get(BlogProperties.RSS_POSTS.getValue());
        if (StrUtil.isBlank(rssPosts)) {
            rssPosts = "20";
        }
        final Sort sort = new Sort(Sort.Direction.DESC, "postDate");
        final Pageable pageable = PageRequest.of(0, Integer.parseInt(rssPosts), sort);
        final Page<Post> postsPage = postService.pageBy(PostStatus.PUBLISHED, PostType.POST, pageable).map(post -> {
            if (StrUtil.isNotEmpty(post.getPassword())) {
                post.setFormatContent("该文章为加密文章");
                post.setSummary("该文章为加密文章");
            }
            return post;
        });
        final List<Post> posts = postsPage.getContent();
        model.addAttribute("posts", posts);
        final Template template = freeMarker.getConfiguration().getTemplate("common/web/rss.ftl");
        return FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
    }

    /**
     * Get atom.xml
     *
     * @param model model
     * @return String
     * @throws IOException       IOException
     * @throws TemplateException TemplateException
     */
    @GetMapping(value = {"atom", "atom.xml"}, produces = "application/xml;charset=UTF-8")
    @ResponseBody
    public String atom(Model model) throws IOException, TemplateException {
        String rssPosts = OPTIONS.get(BlogProperties.RSS_POSTS.getValue());
        if (StrUtil.isBlank(rssPosts)) {
            rssPosts = "20";
        }
        final Sort sort = new Sort(Sort.Direction.DESC, "postDate");
        final Pageable pageable = PageRequest.of(0, Integer.parseInt(rssPosts), sort);
        final Page<Post> postsPage = postService.pageBy(PostStatus.PUBLISHED, PostType.POST, pageable).map(post -> {
            if (StrUtil.isNotEmpty(post.getPassword())) {
                post.setFormatContent("该文章为加密文章");
                post.setSummary("该文章为加密文章");
            }
            return post;
        });
        final List<Post> posts = postsPage.getContent();
        model.addAttribute("posts", posts);
        final Template template = freeMarker.getConfiguration().getTemplate("common/web/atom.ftl");
        return FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
    }

    /**
     * Get sitemap.xml.
     *
     * @param model model
     * @return String
     * @throws IOException       IOException
     * @throws TemplateException TemplateException
     */
    @GetMapping(value = {"sitemap", "sitemap.xml"}, produces = "application/xml;charset=UTF-8")
    @ResponseBody
    public String sitemapXml(Model model) throws IOException, TemplateException {
        final Page<Post> postsPage = postService.pageBy(PostStatus.PUBLISHED, PostType.POST, null).map(post -> {
            if (StrUtil.isNotEmpty(post.getPassword())) {
                post.setFormatContent("该文章为加密文章");
                post.setSummary("该文章为加密文章");
            }
            return post;
        });
        final List<Post> posts = postsPage.getContent();
        model.addAttribute("posts", posts);
        final Template template = freeMarker.getConfiguration().getTemplate("common/web/sitemap_xml.ftl");
        return FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
    }

    /**
     * Get sitemap.html.
     *
     * @param model model
     * @return String
     */
    @GetMapping(value = "sitemap.html", produces = {"text/html"})
    public String sitemapHtml(Model model) {
        final Page<Post> postsPage = postService.pageBy(PostStatus.PUBLISHED, PostType.POST, null).map(post -> {
            if (StrUtil.isNotEmpty(post.getPassword())) {
                post.setFormatContent("该文章为加密文章");
                post.setSummary("该文章为加密文章");
            }
            return post;
        });
        final List<Post> posts = postsPage.getContent();
        model.addAttribute("posts", posts);
        return "common/web/sitemap_html";
    }

    /**
     * Get robots.
     *
     * @param model model
     * @return String
     * @throws IOException       IOException
     * @throws TemplateException TemplateException
     */
    @GetMapping(value = "robots.txt", produces = {"text/plain"})
    @ResponseBody
    public String robots(Model model) throws IOException, TemplateException {
        final Template template = freeMarker.getConfiguration().getTemplate("common/web/robots.ftl");
        return FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
    }
}
