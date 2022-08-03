package run.halo.app.extension;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.core.util.Json;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.openapi4j.core.exception.ResolutionException;
import org.openapi4j.schema.validator.ValidationData;
import org.openapi4j.schema.validator.v3.SchemaValidator;
import run.halo.app.extension.exception.ExtensionConvertException;
import run.halo.app.extension.exception.SchemaViolationException;
import run.halo.app.extension.store.ExtensionStore;

/**
 * JSON implementation of ExtensionConverter.
 *
 * @author johnniang
 */
@Slf4j
public class JSONExtensionConverter implements ExtensionConverter {

    public final ObjectMapper objectMapper;

    private final SchemeManager schemeManager;

    public JSONExtensionConverter(SchemeManager schemeManager) {
        this.schemeManager = schemeManager;
        this.objectMapper = Json.mapper();
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    @Override
    public <E extends Extension> ExtensionStore convertTo(E extension) {
        var gvk = extension.groupVersionKind();
        var scheme = schemeManager.get(gvk);
        var storeName = ExtensionUtil.buildStoreName(scheme, extension.getMetadata().getName());
        try {
            var data = objectMapper.writeValueAsBytes(extension);

            var extensionJsonNode = objectMapper.valueToTree(extension);

            var validator = new SchemaValidator(null, scheme.openApiSchema());
            var validation = new ValidationData<>(extension);
            validator.validate(extensionJsonNode, validation);
            if (!validation.isValid()) {
                log.debug("Failed to validate Extension: {}, and errors were: {}",
                    extension.getClass(), validation.results());
                throw new SchemaViolationException("Failed to validate Extension "
                    + extension.getClass(), validation.results());
            }

            var version = extension.getMetadata().getVersion();
            return new ExtensionStore(storeName, data, version);
        } catch (IOException e) {
            throw new ExtensionConvertException("Failed write Extension as bytes", e);
        } catch (ResolutionException e) {
            throw new RuntimeException("Failed to create schema validator", e);
        }
    }

    @Override
    public <E extends Extension> E convertFrom(Class<E> type, ExtensionStore extensionStore) {
        try {
            var extension = objectMapper.readValue(extensionStore.getData(), type);
            extension.getMetadata().setVersion(extensionStore.getVersion());
            return extension;
        } catch (IOException e) {
            throw new ExtensionConvertException("Failed to read Extension " + type + " from bytes",
                e);
        }
    }

}
