package run.halo.app.extension;

import com.github.victools.jsonschema.generator.Option;
import com.github.victools.jsonschema.generator.OptionPreset;
import com.github.victools.jsonschema.generator.SchemaGenerator;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigBuilder;
import com.github.victools.jsonschema.generator.SchemaVersion;
import com.github.victools.jsonschema.module.swagger2.Swagger2Module;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.lang.NonNull;
import run.halo.app.extension.exception.ExtensionException;
import run.halo.app.extension.exception.SchemeNotFoundException;

public interface SchemeManager {

    void register(@NonNull Scheme scheme);

    /**
     * Registers an Extension using its type.
     *
     * @param type is Extension type.
     * @param <T> Extension class.
     */
    default <T extends Extension> void register(Class<T> type) {
        register(createSchemeFromType(type));
    }


    void unregister(@NonNull Scheme scheme);

    default int size() {
        return schemes().size();
    }

    @NonNull
    List<Scheme> schemes();

    @NonNull
    default Optional<Scheme> fetch(@NonNull GroupVersionKind gvk) {
        return schemes().stream()
            .filter(scheme -> Objects.equals(scheme.groupVersionKind(), gvk))
            .findFirst();
    }

    @NonNull
    default Scheme get(@NonNull GroupVersionKind gvk) {
        return fetch(gvk).orElseThrow(
            () -> new SchemeNotFoundException("Scheme was not found for " + gvk));
    }

    @NonNull
    default Scheme get(Class<? extends Extension> type) {
        var gvk = getGvkFromType(type);
        return get(new GroupVersionKind(gvk.group(), gvk.version(), gvk.kind()));
    }

    @NonNull
    default Scheme get(Extension ext) {
        var gvk = ext.groupVersionKind();
        return get(gvk);
    }

    @NonNull
    static Scheme createSchemeFromType(@NonNull Class<? extends Extension> type) {
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

    static GVK getGvkFromType(@NonNull Class<? extends Extension> type) {
        var gvk = type.getAnnotation(GVK.class);
        if (gvk == null) {
            throw new ExtensionException(
                String.format("Annotation %s needs to be on Extension %s", GVK.class.getName(),
                    type.getName()));
        }
        return gvk;
    }

}
