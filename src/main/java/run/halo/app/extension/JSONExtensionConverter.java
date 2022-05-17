package run.halo.app.extension;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private final ObjectMapper objectMapper;
    private final JsonSchemaFactory jsonSchemaFactory;

    public JSONExtensionConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        jsonSchemaFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V201909);
    }

    @Override
    public <E extends Extension> ExtensionStore convertTo(E extension) {
        var scheme = Schemes.INSTANCE.get(extension.getClass());
        var storeName = ExtensionUtil.buildStoreName(scheme, extension.metadata().getName());
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("JSON schema({}): {}", scheme.type(),
                    scheme.jsonSchema().toPrettyString());
            }

            var validator = jsonSchemaFactory.getSchema(scheme.jsonSchema());
            var extensionNode = objectMapper.valueToTree(extension);
            var errors = validator.validate(extensionNode);
            if (!CollectionUtils.isEmpty(errors)) {
                if (logger.isDebugEnabled()) {
                    // only print the errors when debug mode is enabled
                    logger.error("Failed to validate Extension {}, and errors are: {}",
                        extension.getClass(), errors);
                }
                throw new SchemaViolationException(
                    "Failed to validate Extension " + extension.getClass(), errors);
            }

            // keep converting
            var data = objectMapper.writeValueAsBytes(extensionNode);
            var version = extension.metadata().getVersion();
            return new ExtensionStore(storeName, data, version);
        } catch (JsonProcessingException e) {
            throw new ExtensionConvertException("Failed write Extension as bytes", e);
        }
    }

    @Override
    public <E extends Extension> E convertFrom(Class<E> type, ExtensionStore extensionStore) {
        try {
            var extension = objectMapper.readValue(extensionStore.getData(), type);
            extension.metadata().setVersion(extensionStore.getVersion());
            return extension;
        } catch (IOException e) {
            throw new ExtensionConvertException("Failed to read Extension " + type + " from bytes",
                e);
        }
    }

}
