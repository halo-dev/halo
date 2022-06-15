package run.halo.app.extension;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.victools.jsonschema.generator.Option;
import com.github.victools.jsonschema.generator.OptionPreset;
import com.github.victools.jsonschema.generator.SchemaGenerator;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigBuilder;
import com.github.victools.jsonschema.generator.SchemaVersion;
import com.github.victools.jsonschema.module.swagger2.Swagger2Module;
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
 * @param jsonSchema is JSON schema of Extension.
 * @author johnniang
 */
public record Scheme(Class<? extends Extension> type,
                     GroupVersionKind groupVersionKind,
                     String plural,
                     String singular,
                     ObjectNode jsonSchema) {
    public Scheme {
        Assert.notNull(type, "Type of Extension must not be null");
        Assert.notNull(groupVersionKind, "GroupVersionKind of Extension must not be null");
        Assert.hasText(plural, "Plural name of Extension must not be blank");
        Assert.hasText(singular, "Singular name of Extension must not be blank");
        Assert.notNull(jsonSchema, "Json Schema must not be null");
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
        // generate JSON schema
        var module = new Swagger2Module();
        var config =
            new SchemaGeneratorConfigBuilder(SchemaVersion.DRAFT_2019_09, OptionPreset.PLAIN_JSON)
                .with(
                    // See https://victools.github.io/jsonschema-generator/#generator-options
                    // fore more.
                    Option.INLINE_ALL_SCHEMAS,
                    Option.MAP_VALUES_AS_ADDITIONAL_PROPERTIES
                )
                .with(module)
                .build();
        var generator = new SchemaGenerator(config);
        var jsonSchema = generator.generateSchema(type);

        return new Scheme(type,
            new GroupVersionKind(gvk.group(), gvk.version(), gvk.kind()),
            gvk.plural(),
            gvk.singular(),
            jsonSchema);
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
        if (gvk == null) {
            throw new ExtensionException(
                String.format("Annotation %s needs to be on Extension %s", GVK.class.getName(),
                    type.getName()));
        }
        return gvk;
    }
}
