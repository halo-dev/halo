package cc.ryanc.halo.config;

import cc.ryanc.halo.web.interceptor.InstallInterceptor;
import cc.ryanc.halo.web.interceptor.LoginInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author : RYAN0UP
 * @date : 2018/1/2
 * @version : 1.0
 */
@Slf4j
@Configuration
@EnableWebMvc
@ComponentScan(basePackages = "cc.ryanc.halo.web.controller")
@PropertySource(value = "classpath:application.yaml",ignoreResourceNotFound = true,encoding = "UTF-8")
public class MvcConfig implements WebMvcConfigurer {

    @Autowired
    private LoginInterceptor loginInterceptor;

    @Autowired
    private InstallInterceptor installInterceptor;

    /**
     * 注册拦截器
     * @param registry registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/admin/**")
                .excludePathPatterns("/admin/login")
                .excludePathPatterns("/admin/getLogin")
                .excludePathPatterns("/static/**");
        registry.addInterceptor(installInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/install")
                .excludePathPatterns("/install/do")
                .excludePathPatterns("/static/**");
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
        registry.addResourceHandler("/favicon.ico")
                .addResourceLocations("classpath:/static/images/favicon.ico");
    }
}
