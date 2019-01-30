package cc.ryanc.halo.model.freemarker.method;

import cc.ryanc.halo.service.PostService;
import freemarker.template.SimpleNumber;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author : RYAN0UP
 * @date : 2018/12/31
 */
@Component
public class RecentPostsMethod implements TemplateMethodModelEx {

    @Autowired
    private PostService postService;

    /**
     * 获取最近的文章
     *
     * @param arguments 参数
     * @return Object
     * @throws TemplateModelException TemplateModelException
     */
    @Override
    public Object exec(List arguments) throws TemplateModelException {
        SimpleNumber argOne = (SimpleNumber) arguments.get(0);
        int limit = argOne.getAsNumber().intValue();
        return postService.getRecentPosts(limit);
    }
}
