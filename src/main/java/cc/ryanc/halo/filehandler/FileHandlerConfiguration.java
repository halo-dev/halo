package cc.ryanc.halo.filehandler;

import cc.ryanc.halo.config.properties.HaloProperties;
import cc.ryanc.halo.service.OptionService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * File handler configuration.
 *
 * @author johnniang
 * @date 3/27/19
 */
@Configuration
public class FileHandlerConfiguration {

    @Bean
    @ConditionalOnMissingBean
    FileHandler localFileHandler(OptionService optionService, HaloProperties haloProperties) {
        return new LocalFileHandler(optionService, haloProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    FileHandler qnYunFileHandler(OptionService optionService) {
        return new QnYunFileHandler(optionService);
    }

    @Bean
    @ConditionalOnMissingBean
    FileHandler upYunFileHandler(OptionService optionService) {
        return new UpYunFileHandler(optionService);
    }
}
