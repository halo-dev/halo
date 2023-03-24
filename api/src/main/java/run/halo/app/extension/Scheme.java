package run.halo.app.extension;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.core.util.Json;
import java.util.Map;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;
import run.halo.app.extension.exception.ExtensionException;

/**
 * This class represents scheme of an Extension.
 *
 * @param type is Extension type.
 * @param groupVersionKind is GroupVersionKind of Extension.
 * @param plural is plural name of Extension.
 * @param singular is singular name of Extension.
 * @param openApiSchema is JSON schema of Extension.
 * @author johnniang
 */
public record Scheme(Class<? extends Extension> type,
                     GroupVersionKind groupVersionKind,
                     String plural,
                     String singular,
                     ObjectNode openApiSchema) {
    public Scheme {
        Assert.notNull(type, "Type of Extension must not be null");
        Assert.notNull(groupVersionKind, "GroupVersionKind of Extension must not be null");
        Assert.hasText(plural, "Plural name of Extension must not be blank");
        Assert.hasText(singular, "Singular name of Extension must not be blank");
        Assert.notNull(openApiSchema, "Json Schema must not be null");
    }

    /**
     * Builds Scheme from type with @GVK annotation.
     *
     * @param type is Extension type with GVK annotation.
     * @return Scheme definition.
     * @throws ExtensionException when the type has not annotated @GVK.
     */
    public static Scheme buildFromType(Class<? extends Extension> type) {
        // concrete scheme from annotation
        var gvk = getGvkFromType(type);

        // TODO Move the generation logic outside.
        // generate OpenAPI schema
        var resolvedSchema = ModelConverters.getInstance().readAllAsResolvedSchema(type);
        var mapper = Json.mapper();
        var schema = (ObjectNode) mapper.valueToTree(resolvedSchema.schema);
        // for schema validation.
        schema.set("components",
            mapper.valueToTree(Map.of("schemas", resolvedSchema.referencedSchemas)));

        return new Scheme(type,
            new GroupVersionKind(gvk.group(), gvk.version(), gvk.kind()),
            gvk.plural(),
            gvk.singular(),
            schema);
    }

    /**
     * Gets GVK annotation from Extension type.
     *
     * @param type is Extension type with GVK annotation.
     * @return GVK annotation.
     * @throws ExtensionException when the type has not annotated @GVK.
     */
    @NonNull
    public static GVK getGvkFromType(@NonNull Class<? extends Extension> type) {
        var gvk = type.getAnnotation(GVK.class);
        Assert.notNull(gvk,
            "Missing annotation " + GVK.class.getName() + " on type " + type.getName());
        return gvk;
    }
}
