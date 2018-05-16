package cc.ryanc.halo.web.controller.front;

import cc.ryanc.halo.model.domain.Category;
import cc.ryanc.halo.model.domain.Post;
import cc.ryanc.halo.model.dto.HaloConst;
import cc.ryanc.halo.service.CategoryService;
import cc.ryanc.halo.service.PostService;
import cc.ryanc.halo.web.controller.core.BaseController;
import org.apache.commons.lang3.StringUtils;
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

import java.util.List;

/**
 * @author : RYAN0UP
 * @version : 1.0
 * @date : 2018/4/26
 */
@Controller
@RequestMapping(value = "categories")
public class FrontCategoryController extends BaseController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private PostService postService;

    public String categories(Model model){
        List<Category> categories = categoryService.findAllCategories();
        model.addAttribute("categories",categories);
        return this.render("categories");
    }

    /**
     * 根据分类路径查询文章
     *
     * @param model   model
     * @param cateUrl cateUrl
     * @return string
     */
    @GetMapping(value = "{cateUrl}")
    public String categories(Model model,
                             @PathVariable("cateUrl") String cateUrl) {
        return this.categories(model,cateUrl,1);
    }

    /**
     * 根据分类目录查询所有文章 分页
     *
     * @param model model
     * @param cateUrl 分类目录路径
     * @param page 页码
     * @return string
     */
    @GetMapping("{cateUrl}/page/{page}")
    public String categories(Model model,
                             @PathVariable("cateUrl") String cateUrl,
                             @PathVariable("page") Integer page){
        Category category = categoryService.findByCateUrl(cateUrl);
        if(null==category){
            return this.renderNotFound();
        }
        Sort sort = new Sort(Sort.Direction.DESC,"postDate");
        Integer size = 10;
        if(!StringUtils.isBlank(HaloConst.OPTIONS.get("index_posts"))){
            size = Integer.parseInt(HaloConst.OPTIONS.get("index_posts"));
        }
        Pageable pageable = PageRequest.of(page-1,size,sort);
        Page<Post> posts = postService.findPostByCategories(category,pageable);
        model.addAttribute("posts",posts);
        model.addAttribute("category",category);
        return this.render("category");
    }
}
