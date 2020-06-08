package run.halo.app.theme;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.springframework.lang.NonNull;

/**
 * Yaml resolver.
 *
 * @author johnniang
 */
public enum YamlResolver {

    INSTANCE;

    private final ObjectMapper yamlMapper;

    YamlResolver() {
        // create a default yaml mapper
        yamlMapper = new ObjectMapper(new YAMLFactory());
        yamlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * Get yaml mapper.
     *
     * @return non-null yaml mapper
     */
    @NonNull
    public ObjectMapper getYamlMapper() {
        return yamlMapper;
    }
}
