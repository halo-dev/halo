package cc.ryanc.halo.web.controller.front;

import cc.ryanc.halo.model.domain.Post;
import cc.ryanc.halo.model.dto.HaloConst;
import cc.ryanc.halo.model.enums.BlogPropertiesEnum;
import cc.ryanc.halo.model.enums.PostTypeEnum;
import cc.ryanc.halo.service.PostService;
import cn.hutool.core.util.StrUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

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

    /**
     * 获取文章rss
     *
     * @return rss
     */
    @GetMapping(value = {"feed", "feed.xml", "atom", "atom.xml"}, produces = "application/xml;charset=UTF-8")
    @ResponseBody
    public String feed() {
        String rssPosts = HaloConst.OPTIONS.get(BlogPropertiesEnum.RSS_POSTS.getProp());
        if (StrUtil.isBlank(rssPosts)) {
            rssPosts = "20";
        }
        //获取文章列表并根据时间排序
        final Sort sort = new Sort(Sort.Direction.DESC, "postDate");
        final Pageable pageable = PageRequest.of(0, Integer.parseInt(rssPosts), sort);
        final Page<Post> postsPage = postService.findPostByStatus(0, PostTypeEnum.POST_TYPE_POST.getDesc(), pageable);
        final List<Post> posts = postsPage.getContent();
        return postService.buildRss(posts);
    }

    /**
     * 获取sitemap
     *
     * @return sitemap
     */
    @GetMapping(value = {"sitemap", "sitemap.xml"}, produces = "application/xml;charset=UTF-8")
    @ResponseBody
    public String siteMap() {
        //获取文章列表并根据时间排序
        final Sort sort = new Sort(Sort.Direction.DESC, "postDate");
        final Pageable pageable = PageRequest.of(0, 999, sort);
        final Page<Post> postsPage = postService.findPostByStatus(0, PostTypeEnum.POST_TYPE_POST.getDesc(), pageable);
        final List<Post> posts = postsPage.getContent();
        return postService.buildSiteMap(posts);
    }
}
