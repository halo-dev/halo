package run.halo.app.extension;

import com.github.victools.jsonschema.generator.Option;
import com.github.victools.jsonschema.generator.OptionPreset;
import com.github.victools.jsonschema.generator.SchemaGenerator;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigBuilder;
import com.github.victools.jsonschema.generator.SchemaVersion;
import com.github.victools.jsonschema.module.swagger2.Swagger2Module;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import run.halo.app.extension.exception.ExtensionException;
import run.halo.app.extension.exception.SchemeNotFoundException;

/**
 * Schemes is aggregation of schemes and responsible for managing and organizing schemes.
 *
 * @author johnniang
 */
public enum Schemes {

    INSTANCE;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * All registered schemes.
     */
    private final Set<Scheme> schemes;

    /**
     * The map mapping type and scheme of Extension.
     */
    private final Map<Class<? extends Extension>, Scheme> typeToScheme;

    /**
     * The map mapping GroupVersionKind and type of Extension.
     */
    private final Map<GroupVersionKind, Class<? extends Extension>> gvkToType;

    Schemes() {
        schemes = new HashSet<>();
        typeToScheme = new HashMap<>();
        gvkToType = new HashMap<>();
    }

    /**
     * Clear registered schemes.
     * This method is only for test.
     */
    void clear() {
        schemes.clear();
        typeToScheme.clear();
        gvkToType.clear();
    }

    /**
     * Registers an Extension using its type.
     *
     * @param type is Extension type.
     * @param <T> Extension class.
     */
    public <T extends Extension> void register(Class<T> type) {
        // concrete scheme from annotation
        var gvk = type.getAnnotation(GVK.class);
        if (gvk == null) {
            // should never happen
            throw new ExtensionException(
                String.format("Annotation %s needs to be on Extension %s", GVK.class.getName(),
                    type.getName()));
        }

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

        var scheme = new Scheme(type, new GroupVersionKind(gvk.group(), gvk.version(), gvk.kind()),
            gvk.plural(), gvk.singular(), jsonSchema);

        register(scheme);
    }

    /**
     * Registers a Scheme of Extension.
     *
     * @param scheme is fresh scheme of Extension.
     */
    public void register(Scheme scheme) {
        boolean added = schemes.add(scheme);
        if (!added) {
            logger.warn("Scheme " + scheme
                + " has been registered before, please check the repeat register.");
            return;
        }
        typeToScheme.put(scheme.type(), scheme);
        gvkToType.put(scheme.groupVersionKind(), scheme.type());
    }

    /**
     * Fetches a Scheme using Extension type.
     *
     * @param type is Extension type.
     * @return an optional Scheme.
     */
    public Optional<Scheme> fetch(Class<? extends Extension> type) {
        return Optional.ofNullable(typeToScheme.get(type));
    }


    /**
     * Gets a scheme using Extension type.
     *
     * @param type is Extension type.
     * @return non-null Extension scheme.
     * @throws SchemeNotFoundException when the Extension is not found.
     */
    public Scheme get(Class<? extends Extension> type) {
        return fetch(type).orElseThrow(() -> new SchemeNotFoundException(
            "Scheme was not found for Extension " + type.getSimpleName()));
    }

}
