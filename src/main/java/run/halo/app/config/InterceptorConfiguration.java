package run.halo.app.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import run.halo.app.interceptor.RecordInterceptor;

@EnableWebMvc
@Configuration
public class InterceptorConfiguration implements WebMvcConfigurer {

    private final RecordInterceptor recordInterceptor;

    public InterceptorConfiguration(RecordInterceptor recordInterceptor) {
        this.recordInterceptor = recordInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(recordInterceptor).addPathPatterns("/**");
    }
}
