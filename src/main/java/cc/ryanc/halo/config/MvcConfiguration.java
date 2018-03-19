package cc.ryanc.halo.config;

import cc.ryanc.halo.web.interceptor.InstallInterceptor;
import cc.ryanc.halo.web.interceptor.LoginInterceptor;
import cc.ryanc.halo.web.interceptor.CommonInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.servlet.config.annotation.*;

/**
 * @author : RYAN0UP
 * @version : 1.0
 * @date : 2018/1/2
 * description:
 */
@Slf4j
@Configuration
@EnableWebMvc
@ComponentScan(basePackages = "cc.ryanc.halo.web.controller")
@PropertySource(value = "classpath:application.yaml",ignoreResourceNotFound = true,encoding = "UTF-8")
public class MvcConfiguration extends WebMvcConfigurerAdapter {

    @Autowired
    private LoginInterceptor loginInterceptor;

    @Autowired
    private InstallInterceptor installInterceptor;

    @Autowired
    private CommonInterceptor commonInterceptor;

    /**
     * 注册拦截器
     * @param registry registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/admin/**")
                .excludePathPatterns("/admin/login")
                .excludePathPatterns("/admin/getLogin");
        //registry.addInterceptor(installInterceptor)
        //        .addPathPatterns("/**")
        //        .excludePathPatterns("/install")
        //        .excludePathPatterns("/install/do");
        registry.addInterceptor(commonInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/admin/getLogin")
                .excludePathPatterns("/admin/attachments/upload")
                .excludePathPatterns("/admin/attachments/remove")
                .excludePathPatterns("/admin/category/checkUrl")
                .excludePathPatterns("/admin/option/save")
                .excludePathPatterns("/admin/posts/new/push")
                .excludePathPatterns("/admin/posts/updateSummary")
                .excludePathPatterns("/admin/posts/checkUrl")
                .excludePathPatterns("/admin/tag/checkUrl")
                .excludePathPatterns("/admin/themes/set")
                .excludePathPatterns("/admin/themes/upload")
                .excludePathPatterns("/admin/themes/getTpl")
                .excludePathPatterns("/admin/themes/editor/save")
                .excludePathPatterns("/admin/profile/save")
                .excludePathPatterns("/admin/profile/changePass")
                .excludePathPatterns("/next")
                .excludePathPatterns("/feed")
                .excludePathPatterns("/feed.xml")
                .excludePathPatterns("/atom.xml")
                .excludePathPatterns("/sitemap")
                .excludePathPatterns("/sitemap.xml")
                .excludePathPatterns("/newComment")
                .excludePathPatterns("/install/do")
                .excludePathPatterns("/getComment/{postId}");
    }

    /**
     * 配置静态资源路径
     * @param registry registry
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/templates/themes/")
                .addResourceLocations("classpath:/robots.txt");
        registry.addResourceHandler("/upload/**")
                .addResourceLocations("classpath:/upload/");
    }
}
