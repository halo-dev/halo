package cc.ryanc.halo.config;

import cc.ryanc.halo.model.freemarker.method.RandomMethod;
import cc.ryanc.halo.model.freemarker.method.RecentCommentsMethod;
import cc.ryanc.halo.model.freemarker.method.RecentPostsMethod;
import cc.ryanc.halo.model.freemarker.tag.*;
import cc.ryanc.halo.service.OptionService;
import freemarker.template.TemplateModelException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * FreeMarker configuration.
 *
 * @author : RYAN0UP
 * @date : 2018/4/26
 */
@Slf4j
@Configuration
public class FreeMarkerAutoConfiguration {

    @Autowired
    private freemarker.template.Configuration configuration;

    @Autowired
    private OptionService optionsService;

    @Autowired
    private PostTagDirective postTagDirective;

    @Autowired
    private CategoryTagDirective categoryTagDirective;

    @Autowired
    private CommentTagDirective commentTagDirective;

    @Autowired
    private LinkTagDirective linkTagDirective;

    @Autowired
    private MenuTagDirective menuTagDirective;

    @Autowired
    private TagTagDirective tagTagDirective;

    @Autowired
    private RandomMethod randomMethod;

    @Autowired
    private RecentPostsMethod recentPostsMethod;

    @Autowired
    private RecentCommentsMethod recentCommentsMethod;

    @PostConstruct
    public void setSharedVariable() {
        try {
            configuration.setSharedVariable("options", optionsService.listOptions());
            //Freemarker custom tags
            configuration.setSharedVariable("categoryTag", categoryTagDirective);
            configuration.setSharedVariable("commentTag", commentTagDirective);
            configuration.setSharedVariable("linkTag", linkTagDirective);
            configuration.setSharedVariable("menuTag", menuTagDirective);
            configuration.setSharedVariable("tagTag", tagTagDirective);
            configuration.setSharedVariable("postTag", postTagDirective);
            configuration.setSharedVariable("randomMethod", randomMethod);
            configuration.setSharedVariable("recentPostsMethod", recentPostsMethod);
            configuration.setSharedVariable("recentCommentsMethod", recentCommentsMethod);
        } catch (TemplateModelException e) {
            log.error("Custom tags failed to load：{}", e.getMessage());
        }
    }
}
