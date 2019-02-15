package cc.ryanc.halo.web.controller.front;

import cc.ryanc.halo.model.domain.Post;
import cc.ryanc.halo.model.enums.BlogPropertiesEnum;
import cc.ryanc.halo.model.enums.PostTypeEnum;
import cc.ryanc.halo.service.PostService;
import cn.hutool.core.util.StrUtil;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.springframework.beans.factory.annotation.Autowired;
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

import static cc.ryanc.halo.model.dto.HaloConst.OPTIONS;

/**
 * <pre>
 *     sitemap，rss页面控制器
 * </pre>
 *
 * @author : RYAN0UP
 * @date : 2018/4/26
 */
@Controller
public class FrontOthersController {

    @Autowired
    private PostService postService;

    @Autowired
    private FreeMarkerConfigurer freeMarker;

    /**
     * 获取文章rss
     *
     * @param model model
     *
     * @return String
     *
     * @throws IOException       IOException
     * @throws TemplateException TemplateException
     */
    @GetMapping(value = {"feed", "feed.xml", "rss", "rss.xml"}, produces = "application/xml;charset=UTF-8")
    @ResponseBody
    public String feed(Model model) throws IOException, TemplateException {
        String rssPosts = OPTIONS.get(BlogPropertiesEnum.RSS_POSTS.getProp());
        if (StrUtil.isBlank(rssPosts)) {
            rssPosts = "20";
        }
        final Sort sort = new Sort(Sort.Direction.DESC, "postDate");
        final Pageable pageable = PageRequest.of(0, Integer.parseInt(rssPosts), sort);
        final Page<Post> postsPage = postService.findPostByStatus(0, PostTypeEnum.POST_TYPE_POST.getDesc(), pageable).map(post -> {
            if (StrUtil.isNotEmpty(post.getPostPassword())) {
                post.setPostContent("该文章为加密文章");
                post.setPostSummary("该文章为加密文章");
            }
            return post;
        });
        final List<Post> posts = postsPage.getContent();
        model.addAttribute("posts", posts);
        final Template template = freeMarker.getConfiguration().getTemplate("common/web/rss.ftl");
        return FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
    }

    /**
     * 获取 atom.xml
     *
     * @param model model
     *
     * @return String
     *
     * @throws IOException       IOException
     * @throws TemplateException TemplateException
     */
    @GetMapping(value = {"atom", "atom.xml"}, produces = "application/xml;charset=UTF-8")
    @ResponseBody
    public String atom(Model model) throws IOException, TemplateException {
        String rssPosts = OPTIONS.get(BlogPropertiesEnum.RSS_POSTS.getProp());
        if (StrUtil.isBlank(rssPosts)) {
            rssPosts = "20";
        }
        final Sort sort = new Sort(Sort.Direction.DESC, "postDate");
        final Pageable pageable = PageRequest.of(0, Integer.parseInt(rssPosts), sort);
        final Page<Post> postsPage = postService.findPostByStatus(0, PostTypeEnum.POST_TYPE_POST.getDesc(), pageable).map(post -> {
            if (StrUtil.isNotEmpty(post.getPostPassword())) {
                post.setPostContent("该文章为加密文章");
                post.setPostSummary("该文章为加密文章");
            }
            return post;
        });
        final List<Post> posts = postsPage.getContent();
        model.addAttribute("posts", posts);
        final Template template = freeMarker.getConfiguration().getTemplate("common/web/atom.ftl");
        return FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
    }

    /**
     * 获取 XML 格式的站点地图
     *
     * @param model model
     *
     * @return String
     *
     * @throws IOException       IOException
     * @throws TemplateException TemplateException
     */
    @GetMapping(value = {"sitemap", "sitemap.xml"}, produces = "application/xml;charset=UTF-8")
    @ResponseBody
    public String sitemapXml(Model model) throws IOException, TemplateException {
        final Page<Post> postsPage = postService.findPostByStatus(0, PostTypeEnum.POST_TYPE_POST.getDesc(), null).map(post -> {
            if (StrUtil.isNotEmpty(post.getPostPassword())) {
                post.setPostContent("该文章为加密文章");
                post.setPostSummary("该文章为加密文章");
            }
            return post;
        });
        final List<Post> posts = postsPage.getContent();
        model.addAttribute("posts", posts);
        final Template template = freeMarker.getConfiguration().getTemplate("common/web/sitemap_xml.ftl");
        return FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
    }

    /**
     * 获取 HTML 格式的站点地图
     *
     * @param model model
     *
     * @return String
     */
    @GetMapping(value = "sitemap.html", produces = {"text/html"})
    public String sitemapHtml(Model model) {
        final Page<Post> postsPage = postService.findPostByStatus(0, PostTypeEnum.POST_TYPE_POST.getDesc(), null).map(post -> {
            if (StrUtil.isNotEmpty(post.getPostPassword())) {
                post.setPostContent("该文章为加密文章");
                post.setPostSummary("该文章为加密文章");
            }
            return post;
        });
        final List<Post> posts = postsPage.getContent();
        model.addAttribute("posts", posts);
        return "common/web/sitemap_html";
    }

    /**
     * robots
     *
     * @param model model
     *
     * @return String
     *
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
