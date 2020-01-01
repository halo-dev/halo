package run.halo.app.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import freemarker.core.TemplateClassResolver;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.jackson.JsonComponentModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.domain.PageImpl;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewResolverRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
import org.springframework.web.servlet.view.freemarker.FreeMarkerViewResolver;
import run.halo.app.config.properties.HaloProperties;
import run.halo.app.core.PageJacksonSerializer;
import run.halo.app.factory.StringToEnumConverterFactory;
import run.halo.app.model.support.HaloConst;
import run.halo.app.security.resolver.AuthenticationArgumentResolver;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import static run.halo.app.model.support.HaloConst.FILE_SEPARATOR;
import static run.halo.app.model.support.HaloConst.HALO_ADMIN_RELATIVE_PATH;
import static run.halo.app.utils.HaloUtils.*;

/**
 * Mvc configuration.
 *
 * @author ryanwang
 * @date 2018-01-02
 */
@Slf4j
@Configuration
@EnableWebMvc
@ComponentScan(basePackages = "run.halo.app.controller")
@PropertySource(value = "classpath:application.yaml", ignoreResourceNotFound = true, encoding = "UTF-8")
public class WebMvcAutoConfiguration implements WebMvcConfigurer {

    private static final String FILE_PROTOCOL = "file:///";

    private final HaloProperties haloProperties;

    public WebMvcAutoConfiguration(HaloProperties haloProperties) {
        this.haloProperties = haloProperties;
    }

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.stream()
                .filter(c -> c instanceof MappingJackson2HttpMessageConverter)
                .findFirst().ifPresent(converter -> {
            MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter = (MappingJackson2HttpMessageConverter) converter;
            Jackson2ObjectMapperBuilder builder = Jackson2ObjectMapperBuilder.json();
            JsonComponentModule module = new JsonComponentModule();
            module.addSerializer(PageImpl.class, new PageJacksonSerializer());
            ObjectMapper objectMapper = builder.modules(module).build();
            mappingJackson2HttpMessageConverter.setObjectMapper(objectMapper);
        });
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new AuthenticationArgumentResolver());
    }

    /**
     * Configuring static resource path
     *
     * @param registry registry
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String workDir = FILE_PROTOCOL + ensureSuffix(haloProperties.getWorkDir(), FILE_SEPARATOR);
        String backupDir = FILE_PROTOCOL + ensureSuffix(haloProperties.getBackupDir(), FILE_SEPARATOR);
        registry.addResourceHandler("/**")
                .addResourceLocations(workDir + "templates/themes/")
                .addResourceLocations(workDir + "templates/admin/")
                .addResourceLocations("classpath:/admin/")
                .addResourceLocations(workDir + "static/");

        String uploadUrlPattern = ensureBoth(haloProperties.getUploadUrlPrefix(), URL_SEPARATOR) + "**";
        String adminPathPattern = ensureSuffix(haloProperties.getAdminPath(), URL_SEPARATOR) + "**";

        registry.addResourceHandler(uploadUrlPattern)
                .addResourceLocations(workDir + "upload/");
        registry.addResourceHandler(adminPathPattern)
                .addResourceLocations(workDir + HALO_ADMIN_RELATIVE_PATH)
                .addResourceLocations("classpath:/admin/");

        if (!haloProperties.isDocDisabled()) {
            // If doc is enable
            registry.addResourceHandler("swagger-ui.html")
                    .addResourceLocations("classpath:/META-INF/resources/");
            registry.addResourceHandler("/webjars/**")
                    .addResourceLocations("classpath:/META-INF/resources/webjars/");
        }
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverterFactory(new StringToEnumConverterFactory());
    }

    /**
     * Configuring freemarker template file path.
     *
     * @return new FreeMarkerConfigurer
     */
    @Bean
    public FreeMarkerConfigurer freemarkerConfig(HaloProperties haloProperties) throws IOException, TemplateException {
        FreeMarkerConfigurer configurer = new FreeMarkerConfigurer();
        configurer.setTemplateLoaderPaths(FILE_PROTOCOL + haloProperties.getWorkDir() + "templates/", "classpath:/templates/");
        configurer.setDefaultEncoding("UTF-8");

        Properties properties = new Properties();
        properties.setProperty("auto_import", "/common/macro/common_macro.ftl as common,/common/macro/global_macro.ftl as global");

        configurer.setFreemarkerSettings(properties);

        // Predefine configuration
        freemarker.template.Configuration configuration = configurer.createConfiguration();

        configuration.setNewBuiltinClassResolver(TemplateClassResolver.SAFER_RESOLVER);

        if (haloProperties.isProductionEnv()) {
            configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        }

        // Set predefined freemarker configuration
        configurer.setConfiguration(configuration);

        return configurer;
    }

    /**
     * Configuring view resolver
     *
     * @param registry registry
     */
    @Override
    public void configureViewResolvers(ViewResolverRegistry registry) {
        FreeMarkerViewResolver resolver = new FreeMarkerViewResolver();
        resolver.setAllowRequestOverride(false);
        resolver.setCache(false);
        resolver.setExposeRequestAttributes(false);
        resolver.setExposeSessionAttributes(false);
        resolver.setExposeSpringMacroHelpers(true);
        resolver.setSuffix(HaloConst.SUFFIX_FTL);
        resolver.setContentType("text/html; charset=UTF-8");
        registry.viewResolver(resolver);
    }
}
