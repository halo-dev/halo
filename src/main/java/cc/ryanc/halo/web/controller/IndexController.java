package cc.ryanc.halo.web.controller;

import cc.ryanc.halo.model.domain.Category;
import cc.ryanc.halo.model.domain.Link;
import cc.ryanc.halo.model.domain.Post;
import cc.ryanc.halo.model.domain.Tag;
import cc.ryanc.halo.model.dto.Archive;
import cc.ryanc.halo.model.dto.HaloConst;
import cc.ryanc.halo.service.CategoryService;
import cc.ryanc.halo.service.LinkService;
import cc.ryanc.halo.service.PostService;
import cc.ryanc.halo.service.TagService;
import cc.ryanc.halo.util.HaloUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.websocket.server.PathParam;
import java.util.Date;
import java.util.List;


/**
 * @author : RYAN0UP
 * @date : 2017/11/23
 * @version : 1.0
 * description : 首页控制器
 */
@Slf4j
@Controller
@RequestMapping(value = {"/","index"})
public class IndexController extends BaseController{

    @Autowired
    private PostService postService;

    @Autowired
    private LinkService linkService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private TagService tagService;


    /**
     * 请求首页
     * @param model model
     * @return freemarker
     */
    @GetMapping
    public String index(Model model){
        //调用方法渲染首页
        return this.index(model,1);
    }

    /**
     * 首页分页
     * @param model model
     * @param page page
     * @param size size
     * @return freemarker
     */
    @GetMapping(value = "page/{page}")
    public String index(Model model,
                        @PathVariable(value = "page") Integer page){
        Sort sort = new Sort(Sort.Direction.DESC,"postDate");
        //默认显示10条
        Integer size = 10;
        //尝试加载设置选项，用于设置显示条数
        if(HaloUtil.isNotNull(HaloConst.OPTIONS.get("index_posts"))){
            size = Integer.parseInt(HaloConst.OPTIONS.get("index_posts"));
        }

        //所有文章数据，分页
        Pageable pageable = new PageRequest(page-1,size,sort);
        Page<Post> posts = postService.findPostByStatus(0,pageable);
        model.addAttribute("posts",posts);

        //系统设置
        model.addAttribute("options",HaloConst.OPTIONS);

        //用户信息
        model.addAttribute("user",HaloConst.USER);

        //所有分类目录
        List<Category> categories = categoryService.findAllCategories();
        model.addAttribute("categories",categories);

        //归档数据，包含[year,month,List<Post>]
        List<Archive> archives = postService.findPostGroupByPostDate();
        model.addAttribute("archives",archives);
        return this.render("index");
    }

    /**
     * ajax分页
     * @param page
     * @return
     */
    @GetMapping(value = "next")
    @ResponseBody
    public List<Post> ajaxIndex(@PathParam(value = "page") Integer page){
        Sort sort = new Sort(Sort.Direction.DESC,"postDate");
        //默认显示10条
        Integer size = 10;
        //尝试加载设置选项，用于设置显示条数
        if(HaloUtil.isNotNull(HaloConst.OPTIONS.get("index_posts"))){
            size = Integer.parseInt(HaloConst.OPTIONS.get("index_posts"));
        }

        //文章数据，只获取文章，没有分页
        Pageable pageable = new PageRequest(page-1,size,sort);
        List<Post> posts = postService.findPostByStatus(0,pageable).getContent();
        return posts;
    }

    /**
     * 渲染文章详情
     * @param postId postId
     * @param model model
     * @return String
     */
    @GetMapping(value = {"archives/{postUrl}","post/{postUrl}","article/{postUrl}"})
    public String getPost(@PathVariable String postUrl, Model model){
        Post post = postService.findByPostUrl(postUrl);
        //获得当前文章的发布日期
        Date postDate = post.getPostDate();
        try {
            //查询当前文章日期之前的所有文章
            List<Post> beforePosts = postService.findByPostDateBefore(postDate);

            //查询当前文章日期之后的所有文章
            List<Post> afterPosts = postService.findByPostDateAfter(postDate);

            if(null!=beforePosts&&beforePosts.size()>0){
                model.addAttribute("beforePost",beforePosts.get(beforePosts.size()-1));
            }
            if(null!=afterPosts&&afterPosts.size()>0){
                model.addAttribute("afterPost",afterPosts.get(afterPosts.size()-1));
            }
        }catch (Exception e){
            log.error("未知错误："+e.getMessage());
        }
        model.addAttribute("post",post);

        //系统设置
        model.addAttribute("options",HaloConst.OPTIONS);

        //用户信息
        model.addAttribute("user",HaloConst.USER);

        //所有分类目录
        List<Category> categories = categoryService.findAllCategories();
        model.addAttribute("categories",categories);
        return this.render("post");
    }

    /**
     * 渲染关于页面
     * @param model model
     * @return string
     */
    @GetMapping(value = "/about")
    public String about(Model model){
        model.addAttribute("about","709831589");
        model.addAttribute("options",HaloConst.OPTIONS);
        List<Category> categories = categoryService.findAllCategories();
        model.addAttribute("categories",categories);
        return this.render("about");
    }

    /**
     * 跳转到图库页面
     * @return String
     */
    @GetMapping(value = "/gallery")
    public String gallery(Model model){
        //系统设置
        model.addAttribute("options",HaloConst.OPTIONS);
        return this.render("gallery");
    }

    /**
     * 友情链接
     * @return string
     */
    @GetMapping(value = "/links")
    public String links(Model model){

        //所有友情链接
        List<Link> links = linkService.findAllLinks();
        model.addAttribute("links",links);

        //系统设置
        model.addAttribute("options",HaloConst.OPTIONS);

        //所有分类目录
        List<Category> categories = categoryService.findAllCategories();
        model.addAttribute("categories",categories);
        return this.render("links");
    }

    /**
     * 标签
     * @param model model
     * @return string
     */
    @GetMapping(value = "/tags")
    public String tags(Model model){
        //所有标签
        List<Tag> tags = tagService.findAllTags();
        model.addAttribute("tags",tags);

        //所有分类目录
        List<Category> categories = categoryService.findAllCategories();
        model.addAttribute("categories",categories);

        //系统设置
        model.addAttribute("options",HaloConst.OPTIONS);
        return this.render("tags");
    }

    /**
     * 根据分类路径查询文章
     * @param model model
     * @param cateUrl cateUrl
     * @return string
     */
    @GetMapping(value = "categories/{cateUrl}")
    public String categories(Model model,
                             @PathVariable("cateUrl") String cateUrl){
        List<Post> posts;
        return null;
    }

    /**
     * 文章归档
     * @param model model
     * @return string
     */
    @GetMapping(value = "/archives")
    public String archives(Model model){
        return this.archives(model,1);
    }

    /**
     * 文章归档分页
     * @param model model
     * @param page page
     * @return string
     */
    @GetMapping(value = "/archives/page/{page}")
    public String archives(Model model,
                           @PathVariable(value = "page") Integer page){

        //所有文章数据，分页，material主题适用
        Sort sort = new Sort(Sort.Direction.DESC,"postDate");
        Pageable pageable = new PageRequest(page-1,5,sort);
        Page<Post> posts = postService.findPostByStatus(0,pageable);
        model.addAttribute("posts",posts);

        //包含[List<Post>,year,month,count]
        List<Archive> archives = postService.findPostGroupByPostDate();
        model.addAttribute("archives",archives);

        //系统设置
        model.addAttribute("options",HaloConst.OPTIONS);

        //用户信息
        model.addAttribute("user",HaloConst.USER);

        //所有分类目录
        List<Category> categories = categoryService.findAllCategories();
        model.addAttribute("categories",categories);

        //是否是归档页，用于判断输出链接
        model.addAttribute("isArchives","true");
        return this.render("archives");
    }

    /**
     * 文章归档，根据年月
     * @param model model
     * @param year year
     * @param month month
     * @return string
     */
    @GetMapping(value = "/archives/{year}/{month}")
    public String archives(Model model,
                           @PathVariable(value = "year") String year,
                           @PathVariable(value = "month") String month){

        //根据年月查出的文章数据，分页
        Sort sort = new Sort(Sort.Direction.DESC,"post_date");
        Pageable pageable = new PageRequest(0,9999,sort);
        Page<Post> posts = postService.findPostByYearAndMonth(year,month,pageable);
        model.addAttribute("posts",posts);

        //系统设置
        model.addAttribute("options",HaloConst.OPTIONS);

        //用户信息
        model.addAttribute("user",HaloConst.USER);

        //分类目录
        List<Category> categories = categoryService.findAllCategories();
        model.addAttribute("categories",categories);

        //是否是归档页，用于判断输出链接
        model.addAttribute("isArchives","true");
        return this.render("archives");
    }

    /**
     * 获取文章rss
     * @return rss
     */
    @GetMapping(value = {"feed","feed.xml","atom.xml"},produces = { "application/xml;charset=UTF-8" })
    @ResponseBody
    public String feed(){
        String rssPosts = HaloConst.OPTIONS.get("rss_posts");
        if(null==rssPosts || "".equals(rssPosts)){
            rssPosts = "20";
        }
        //获取文章列表并根据时间排序
        Sort sort = new Sort(Sort.Direction.DESC,"postDate");
        Pageable pageable = new PageRequest(0,Integer.parseInt(rssPosts),sort);
        Page<Post> postsPage = postService.findPostByStatus(0,pageable);
        List<Post> posts = postsPage.getContent();
        String rss = "";
        try {
            rss = HaloUtil.getRss(posts);
        }catch (Exception e){
            e.printStackTrace();
        }
        return rss;
    }

    /**
     * 获取sitemap
     * @return sitemap
     */
    @GetMapping(value = {"sitemap","sitemap.xml"},produces = { "application/xml;charset=UTF-8" })
    @ResponseBody
    public String sitemap(){
        //获取文章列表并根据时间排序
        Sort sort = new Sort(Sort.Direction.DESC,"postDate");
        Pageable pageable = new PageRequest(0,999,sort);
        Page<Post> postsPage = postService.findPostByStatus(0,pageable);
        List<Post> posts = postsPage.getContent();
        return HaloUtil.getSiteMap(posts);
    }
}
