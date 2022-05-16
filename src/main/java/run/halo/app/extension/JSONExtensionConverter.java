package run.halo.app.extension;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.springframework.stereotype.Component;
import run.halo.app.extension.exception.ExtensionConvertException;
import run.halo.app.extension.store.ExtensionStore;

/**
 * JSON implementation of ExtensionConverter.
 *
 * @author johnniang
 */
@Component
public class JSONExtensionConverter implements ExtensionConverter {

    private final ObjectMapper objectMapper;

    public JSONExtensionConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public <E extends Extension> ExtensionStore convertTo(E extension) {
        var scheme = Schemes.INSTANCE.get(extension.getClass());
        var storeName = ExtensionUtil.buildStoreName(scheme, extension.metadata().getName());
        try {
            // TODO Validate the extension in ExtensionClient
            // keep converting
            var data = objectMapper.writeValueAsBytes(extension);
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
