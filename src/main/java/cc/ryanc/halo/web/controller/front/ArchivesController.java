package cc.ryanc.halo.web.controller.front;

import cc.ryanc.halo.model.domain.Post;
import cc.ryanc.halo.model.dto.HaloConst;
import cc.ryanc.halo.service.PostService;
import cc.ryanc.halo.web.controller.core.BaseController;
import lombok.extern.slf4j.Slf4j;
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

import java.util.Date;
import java.util.List;

/**
 * @author : RYAN0UP
 * @version : 1.0
 * @date : 2018/4/26
 */
@Slf4j
@Controller
@RequestMapping(value = "/archives")
public class ArchivesController extends BaseController {

    @Autowired
    private PostService postService;

    /**
     * 文章归档
     *
     * @param model model
     * @return 模板路径
     */
    @GetMapping
    public String archives(Model model){
        return this.archives(model,1);
    }

    /**
     * 文章归档分页
     *
     * @param model model
     * @param page page 当前页码
     * @return 模板路径/themes/{theme}/archives
     */
    @GetMapping(value = "page/{page}")
    public String archives(Model model,
                           @PathVariable(value = "page") Integer page){

        //所有文章数据，分页，material主题适用
        Sort sort = new Sort(Sort.Direction.DESC,"postDate");
        Pageable pageable = new PageRequest(page-1,5,sort);
        Page<Post> posts = postService.findPostByStatus(0,HaloConst.POST_TYPE_POST,pageable);
        model.addAttribute("posts",posts);
        return this.render("archives");
    }

    /**
     * 文章归档，根据年月
     *
     * @param model model
     * @param year year 年份
     * @param month month 月份
     * @return 模板路径/themes/{theme}/archives
     */
    @GetMapping(value = "{year}/{month}")
    public String archives(Model model,
                           @PathVariable(value = "year") String year,
                           @PathVariable(value = "month") String month){
        Page<Post> posts = postService.findPostByYearAndMonth(year,month,null);
        model.addAttribute("posts",posts);
        return this.render("archives");
    }

    /**
     * 渲染文章详情
     *
     * @param postUrl 文章路径名
     * @param model model
     * @return 模板路径/themes/{theme}/post
     */
    @GetMapping(value = "{postUrl}")
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
            log.error("未知错误：{0}",e.getMessage());
        }
        model.addAttribute("post",post);

        return this.render("post");
    }
}
