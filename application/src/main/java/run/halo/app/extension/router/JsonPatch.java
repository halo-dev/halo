package run.halo.app.extension.router;

import static io.swagger.v3.oas.models.Components.COMPONENTS_SCHEMAS_REF;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * JSON schema for JSONPatch operations.
 *
 * @author johnniang
 */
public final class JsonPatch {

    private JsonPatch() {}

    public static final String SCHEMA_NAME = "JsonPatch";

    public static void addSchema(Components components) {
        Function<String, io.swagger.v3.oas.models.media.Schema<?>> opSchemaFunc =
            op -> new StringSchema()._enum(List.of(op)).type("string");
        var pathSchema = new StringSchema()
            .description("A JSON Pointer path")
            .pattern("^(/[^/~]*(~[01][^/~]*)*)*$")
            .example("/a/b/c");
        var valueSchema = new Schema<>().description("Value can be any JSON value");
        var operationSchema = new io.swagger.v3.oas.models.media.Schema<>()
            .oneOf(List.of(
                new io.swagger.v3.oas.models.media.Schema<>()
                    .$ref(COMPONENTS_SCHEMAS_REF + "AddOperation"),
                new io.swagger.v3.oas.models.media.Schema<>()
                    .$ref(COMPONENTS_SCHEMAS_REF + "ReplaceOperation"),
                new io.swagger.v3.oas.models.media.Schema<>()
                    .$ref(COMPONENTS_SCHEMAS_REF + "TestOperation"),
                new io.swagger.v3.oas.models.media.Schema<>()
                    .$ref(COMPONENTS_SCHEMAS_REF + "RemoveOperation"),
                new io.swagger.v3.oas.models.media.Schema<>()
                    .$ref(COMPONENTS_SCHEMAS_REF + "MoveOperation"),
                new io.swagger.v3.oas.models.media.Schema<>()
                    .$ref(COMPONENTS_SCHEMAS_REF + "CopyOperation")
            ));

        components.addSchemas("AddOperation", new io.swagger.v3.oas.models.media.ObjectSchema()
            .required(List.of("op", "path", "value"))
            .properties(Map.of(
                "op", opSchemaFunc.apply("add"),
                "path", pathSchema,
                "value", valueSchema
            )))
        ;
        components.addSchemas("ReplaceOperation", new io.swagger.v3.oas.models.media.ObjectSchema()
            .required(List.of("op", "path", "value"))
            .properties(Map.of(
                "op", opSchemaFunc.apply("replace"),
                "path", pathSchema,
                "value", valueSchema
            )))
        ;
        components.addSchemas("TestOperation", new io.swagger.v3.oas.models.media.ObjectSchema()
            .required(List.of("op", "path", "value"))
            .properties(Map.of(
                "op", opSchemaFunc.apply("test"),
                "path", pathSchema,
                "value", valueSchema
            )))
        ;
        components.addSchemas("RemoveOperation", new io.swagger.v3.oas.models.media.ObjectSchema()
            .required(List.of("op", "path"))
            .properties(Map.of(
                "op", opSchemaFunc.apply("remove"),
                "path", pathSchema
            )))
        ;
        components.addSchemas("MoveOperation", new io.swagger.v3.oas.models.media.ObjectSchema()
            .required(List.of("op", "from", "path"))
            .properties(Map.of(
                "op", opSchemaFunc.apply("move"),
                "from", pathSchema
                    .description("A JSON Pointer path pointing to the location to move/copy from."),
                "path", pathSchema
            )))
        ;
        components.addSchemas("CopyOperation", new io.swagger.v3.oas.models.media.ObjectSchema()
            .required(List.of("op", "from", "path"))
            .properties(Map.of(
                "op", opSchemaFunc.apply("copy"),
                "from", pathSchema
                    .description("A JSON Pointer path pointing to the location to move/copy from."),
                "path", pathSchema
            )))
        ;
        components.addSchemas(SCHEMA_NAME, new io.swagger.v3.oas.models.media.ArraySchema()
            .description("JSON schema for JSONPatch operations")
            .uniqueItems(true)
            .minItems(1)
            .items(operationSchema)
        );
    }

}
