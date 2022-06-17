package run.halo.app.extension;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import run.halo.app.extension.exception.ExtensionConvertException;
import run.halo.app.extension.exception.SchemaViolationException;
import run.halo.app.extension.store.ExtensionStore;

/**
 * JSON implementation of ExtensionConverter.
 *
 * @author johnniang
 */
@Component
public class JSONExtensionConverter implements ExtensionConverter {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public static final ObjectMapper OBJECT_MAPPER;
    private final JsonSchemaFactory jsonSchemaFactory;

    private final SchemeManager schemeManager;


    static {
        OBJECT_MAPPER = Jackson2ObjectMapperBuilder.json()
            .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .featuresToDisable(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS)
            .serializationInclusion(JsonInclude.Include.NON_NULL)
            .build();
    }

    public JSONExtensionConverter(SchemeManager schemeManager) {
        this.schemeManager = schemeManager;
        jsonSchemaFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V201909);
    }

    @Override
    public <E extends Extension> ExtensionStore convertTo(E extension) {
        var gvk = extension.groupVersionKind();
        var scheme = schemeManager.get(gvk);
        var storeName = ExtensionUtil.buildStoreName(scheme, extension.getMetadata().getName());
        try {
            var data = OBJECT_MAPPER.writeValueAsBytes(extension);

            var validator = jsonSchemaFactory.getSchema(scheme.jsonSchema());
            var errors = validator.validate(OBJECT_MAPPER.readTree(data));
            if (!CollectionUtils.isEmpty(errors)) {
                if (logger.isDebugEnabled()) {
                    // only print the errors when debug mode is enabled
                    logger.error("Failed to validate Extension {}, and errors are: {}",
                        extension.getClass(), errors);
                }
                throw new SchemaViolationException(
                    "Failed to validate Extension " + extension.getClass(), errors);
            }

            var version = extension.getMetadata().getVersion();
            return new ExtensionStore(storeName, data, version);
        } catch (IOException e) {
            throw new ExtensionConvertException("Failed write Extension as bytes", e);
        }
    }

    @Override
    public <E extends Extension> E convertFrom(Class<E> type, ExtensionStore extensionStore) {
        try {
            var extension = OBJECT_MAPPER.readValue(extensionStore.getData(), type);
            extension.getMetadata().setVersion(extensionStore.getVersion());
            return extension;
        } catch (IOException e) {
            throw new ExtensionConvertException("Failed to read Extension " + type + " from bytes",
                e);
        }
    }

}
