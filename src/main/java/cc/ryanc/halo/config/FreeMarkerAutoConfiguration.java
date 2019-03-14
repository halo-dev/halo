package cc.ryanc.halo.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

/**
 * <pre>
 *     FreeMarker配置
 * </pre>
 *
 * @author : RYAN0UP
 * @date : 2018/4/26
 */
@Slf4j
@Configuration
public class FreeMarkerAutoConfiguration {

//    @Autowired
//    private freemarker.template.Configuration configuration;
//
//    @Autowired
//    private OptionsService optionsService;
//
//    @Autowired
//    private UserService userService;
//
//    @Autowired
//    private CommonTagDirective commonTagDirective;
//
//    @Autowired
//    private ArticleTagDirective articleTagDirective;
//
//    @Autowired
//    private RandomMethod randomMethod;
//
//    @Autowired
//    private RecentPostsMethod recentPostsMethod;
//
//    @Autowired
//    private RecentCommentsMethod recentCommentsMethod;
//
//    @PostConstruct
//    public void setSharedVariable() {
//        try {
//            //自定义标签
//            configuration.setSharedVariable("commonTag", commonTagDirective);
//            configuration.setSharedVariable("articleTag", articleTagDirective);
//            configuration.setSharedVariable("options", optionsService.findAllOptions());
//            configuration.setSharedVariable("user", userService.findUser());
//            configuration.setSharedVariable("randomMethod", randomMethod);
//            configuration.setSharedVariable("recentPostsMethod", recentPostsMethod);
//            configuration.setSharedVariable("recentCommentsMethod", recentCommentsMethod);
//        } catch (TemplateModelException e) {
//            log.error("Custom tags failed to load：{}", e.getMessage());
//        }
//    }
}
