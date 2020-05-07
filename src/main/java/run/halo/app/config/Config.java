package run.halo.app.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import run.halo.app.filter.WrapRequestFilter;
import run.halo.app.service.OptionService;

@Slf4j
@Configuration
public class Config {

    private final OptionService optionService;

    public Config(OptionService optionService) {
        this.optionService = optionService;
    }

    @Bean
    public WrapRequestFilter wrapRequestFilter() {
        return new WrapRequestFilter(optionService);
    }

    @Bean
    public FilterRegistrationBean<WrapRequestFilter> wrapRequestFilterRegistrationBean() {
        FilterRegistrationBean<WrapRequestFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(wrapRequestFilter());
        registrationBean.setName("wrapRequestFilter");
        registrationBean.setOrder(-1000001);
        return registrationBean;
    }
}
