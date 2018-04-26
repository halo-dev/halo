package cc.ryanc.halo.model.tag;

import cc.ryanc.halo.service.PostService;
import freemarker.core.Environment;
import freemarker.template.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

/**
 * @author : RYAN0UP
 * @version : 1.0
 * @date : 2018/4/26
 */
@Component
public class ArticleTagDirective implements TemplateDirectiveModel {

    private static final String METHOD_KEY = "method";

    @Autowired
    private PostService postService;

    @Override
    public void execute(Environment environment, Map map, TemplateModel[] templateModels, TemplateDirectiveBody templateDirectiveBody) throws TemplateException, IOException {
        DefaultObjectWrapperBuilder builder = new DefaultObjectWrapperBuilder(Configuration.VERSION_2_3_25);
        if(map.containsKey(METHOD_KEY)){
            String method = map.get(METHOD_KEY).toString();
            switch (method){
                case "postsCount":
                    environment.setVariable("postsCount",builder.build().wrap(postService.findAllPosts().size()));
                    break;
                default:
                    break;
            }
        }
        templateDirectiveBody.render(environment.getOut());
    }
}