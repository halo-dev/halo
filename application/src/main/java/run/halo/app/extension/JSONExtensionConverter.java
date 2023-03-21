package run.halo.app.extension;

import static org.openapi4j.core.validation.ValidationSeverity.ERROR;
import static org.springframework.util.StringUtils.arrayToCommaDelimitedString;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.core.util.Json;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import lombok.extern.slf4j.Slf4j;
import org.openapi4j.core.exception.ResolutionException;
import org.openapi4j.core.model.v3.OAI3;
import org.openapi4j.core.model.v3.OAI3Context;
import org.openapi4j.core.validation.ValidationResult;
import org.openapi4j.core.validation.ValidationResults;
import org.openapi4j.schema.validator.BaseJsonValidator;
import org.openapi4j.schema.validator.ValidationContext;
import org.openapi4j.schema.validator.ValidationData;
import org.openapi4j.schema.validator.v3.SchemaValidator;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import run.halo.app.extension.exception.ExtensionConvertException;
import run.halo.app.extension.exception.SchemaViolationException;
import run.halo.app.extension.store.ExtensionStore;

/**
 * JSON implementation of ExtensionConverter.
 *
 * @author johnniang
 */
@Slf4j
@Component
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

        try {
            var validation = new ValidationData<>(extension);
            var extensionJsonNode = objectMapper.valueToTree(extension);
            var validator = getValidator(scheme);
            validator.validate(extensionJsonNode, validation);
            if (!validation.isValid()) {
                log.debug("Failed to validate Extension: {}, and errors were: {}",
                    extension.getClass(), validation.results());
                throw new SchemaViolationException(extension.groupVersionKind(),
                    validation.results());
            }

            var version = extension.getMetadata().getVersion();
            var storeName = ExtensionUtil.buildStoreName(scheme, extension.getMetadata().getName());
            var data = objectMapper.writeValueAsBytes(extensionJsonNode);
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

    private SchemaValidator getValidator(Scheme scheme)
        throws MalformedURLException, ResolutionException {
        var context = new ValidationContext<OAI3>(
            new OAI3Context(new URL("file:/"), scheme.openApiSchema()));
        context.setFastFail(false);
        return new SchemaValidator(context, null, scheme.openApiSchema());
    }

    public static class ExtraValidationValidator extends BaseJsonValidator<OAI3> {

        private String[] fieldNames;

        private static final ValidationResult ERR =
            new ValidationResult(ERROR, 1100, "Fields '%s' should not be blank at the same time");

        private static final ValidationResults.CrumbInfo CRUMB_INFO =
            new ValidationResults.CrumbInfo("not-blank-at-least-one", true);

        protected ExtraValidationValidator(ValidationContext<OAI3> context,
            JsonNode schemaNode, JsonNode schemaParentNode,
            SchemaValidator parentSchema) {
            super(context, schemaNode, schemaParentNode, parentSchema);

            var withNode = schemaNode.get("not-blank-at-least-one");
            if (withNode != null && withNode.isTextual()) {
                fieldNames = StringUtils.commaDelimitedListToStringArray(withNode.asText());
                withNode.asText();
            }
        }

        @Override
        public boolean validate(JsonNode valueNode, ValidationData<?> validation) {
            if (fieldNames == null) {
                return false;
            }
            for (var fieldName : fieldNames) {
                JsonNode value = valueNode.get(fieldName);
                if (value != null && value.isTextual() && StringUtils.hasText(value.asText())) {
                    return false;
                }
            }
            // or all of them are blank string
            validation.add(CRUMB_INFO, ERR, arrayToCommaDelimitedString(fieldNames));
            return false;
        }
    }

}
