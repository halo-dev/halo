package cc.ryanc.halo.config;

import cc.ryanc.halo.model.tag.ArticleTagDirective;
import cc.ryanc.halo.model.tag.CommonTagDirective;
import cc.ryanc.halo.service.OptionsService;
import cc.ryanc.halo.service.UserService;
import freemarker.template.TemplateModelException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * @author : RYAN0UP
 * @version : 1.0
 * @date : 2018/4/26
 */
@Configuration
public class FreeMarkerConfig {

    @Autowired
    private freemarker.template.Configuration configuration;

    @Autowired
    private OptionsService optionsService;

    @Autowired
    private UserService userService;

    @Autowired
    private CommonTagDirective commonTagDirective;

    @Autowired
    private ArticleTagDirective articleTagDirective;

    @PostConstruct
    public void setSharedVariable(){
        configuration.setSharedVariable("commonTag",commonTagDirective);
        configuration.setSharedVariable("articleTag",articleTagDirective);
        try{
            configuration.setSharedVariable("options",optionsService.findAllOptions());
            configuration.setSharedVariable("user",userService.findUser());
        }catch (TemplateModelException e){
            e.printStackTrace();
        }
    }
}
