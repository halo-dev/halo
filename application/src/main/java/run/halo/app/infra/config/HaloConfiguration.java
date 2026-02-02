package run.halo.app.infra.config;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import run.halo.app.infra.properties.HaloProperties;
import run.halo.app.search.lucene.LuceneSearchEngine;
import tools.jackson.databind.MapperFeature;

@EnableCaching
@Configuration(proxyBeanMethods = false)
@EnableAsync
public class HaloConfiguration {

    @Bean
    JsonMapperBuilderCustomizer objectMapperCustomizer(
        ObjectProvider<ObjectMapper> objectMapperProvider
    ) {
        return builder -> {
            builder.changeDefaultPropertyInclusion(v ->
                v.withValueInclusion(NON_NULL).withContentInclusion(NON_NULL)
            );
            builder.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);
            builder.addModule(
                new JacksonAdapterModule(objectMapperProvider::getIfAvailable)
            );
        };
    }

    @Bean
    @ConditionalOnProperty(prefix = "halo.search-engine.lucene", name = "enabled",
        havingValue = "true",
        matchIfMissing = true)
    LuceneSearchEngine luceneSearchEngine(HaloProperties haloProperties) throws IOException {
        return new LuceneSearchEngine(haloProperties.getWorkDir()
            .resolve("indices")
            .resolve("halo"));
    }
}