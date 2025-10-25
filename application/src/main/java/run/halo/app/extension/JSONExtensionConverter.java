package run.halo.app.extension;

import static run.halo.app.extension.ExtensionStoreUtil.buildStoreName;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.openapi4j.core.exception.ResolutionException;
import org.openapi4j.core.model.v3.OAI3;
import org.openapi4j.core.model.v3.OAI3Context;
import org.openapi4j.schema.validator.ValidationContext;
import org.openapi4j.schema.validator.ValidationData;
import org.openapi4j.schema.validator.v3.SchemaValidator;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import reactor.core.Exceptions;
import run.halo.app.extension.event.SchemeRemovedEvent;
import run.halo.app.extension.exception.ExtensionConvertException;
import run.halo.app.extension.exception.SchemaViolationException;
import run.halo.app.extension.store.Extensions;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

/**
 * JSON implementation of ExtensionConverter.
 *
 * @author johnniang
 */
@Slf4j
@Component
class JSONExtensionConverter implements ExtensionConverter {

    @Getter
    public JsonMapper jsonMapper;

    private final SchemeManager schemeManager;

    private final ConcurrentMap<Scheme, SchemaValidator> validatorMap = new ConcurrentHashMap<>();

    public JSONExtensionConverter(SchemeManager schemeManager) {
        this.schemeManager = schemeManager;
        setJsonMapper(Unstructured.jsonMapper());
    }

    /**
     * Sets ObjectMapper.
     *
     * @param jsonMapper the object mapper, must not be null
     */
    void setJsonMapper(JsonMapper jsonMapper) {
        Assert.notNull(jsonMapper, "ObjectMapper must not be null");
        this.jsonMapper = jsonMapper;
    }

    @Override
    public <E extends Extension> Extensions convertTo(E extension) {
        var gvk = extension.groupVersionKind();
        var scheme = schemeManager.get(gvk);

        try {
            var convertedExtension = Optional.of(extension)
                .map(item -> scheme.type().isAssignableFrom(item.getClass()) ? item
                    : jsonMapper.convertValue(item, scheme.type())
                )
                .orElseThrow();
            var validation = new ValidationData<>(extension);

            var extensionJsonNode = Unstructured.OBJECT_MAPPER.valueToTree(convertedExtension);
            var validator = getValidator(scheme);
            validator.validate(extensionJsonNode, validation);
            if (!validation.isValid()) {
                log.debug("Failed to validate Extension: {}, and errors were: {}",
                    extension.getClass(), validation.results());
                throw new SchemaViolationException(extension.groupVersionKind(),
                    validation.results());
            }

            var version = extension.getMetadata().getVersion();
            var storeName = buildStoreName(scheme, extension.getMetadata().getName());
            var data = jsonMapper.writeValueAsBytes(convertedExtension);
            return new Extensions(storeName, data, version);
        } catch (IOException e) {
            throw new ExtensionConvertException("Failed write Extension as bytes", e);
        } catch (ResolutionException e) {
            throw new RuntimeException("Failed to create schema validator", e);
        }
    }

    @Override
    public <E extends Extension> E convertFrom(Class<E> type, Extensions extensions) {
        try {
            var extension = jsonMapper.readValue(extensions.getData(), type);
            extension.getMetadata().setVersion(extensions.getVersion());
            return extension;
        } catch (JacksonException e) {
            throw new ExtensionConvertException("Failed to convert Extension from store", e);
        }
    }

    @EventListener
    void onSchemeRemovedEvent(SchemeRemovedEvent event) {
        var removed = validatorMap.remove(event.getScheme());
        if (log.isDebugEnabled()) {
            if (removed == null) {
                log.debug("No available validator found while removing validator for scheme: {}",
                    event.getScheme().groupVersionKind()
                );
            } else {
                log.debug("Removed schema validator {} for scheme: {}",
                    removed, event.getScheme().groupVersionKind()
                );
            }
        }
    }

    private SchemaValidator getValidator(Scheme scheme)
        throws MalformedURLException, ResolutionException {
        return validatorMap.computeIfAbsent(scheme, s -> {
            try {
                var context = new ValidationContext<OAI3>(
                    new OAI3Context(new URL("file:/"), scheme.openApiSchema())
                );
                context.setFastFail(false);
                var validator = new SchemaValidator(context, null, scheme.openApiSchema());
                if (log.isDebugEnabled()) {
                    log.debug("Created schema validator {} for scheme: {}",
                        validator, scheme.groupVersionKind()
                    );
                }
                return validator;
            } catch (ResolutionException | MalformedURLException e) {
                throw Exceptions.propagate(e);
            }
        });
    }

}
