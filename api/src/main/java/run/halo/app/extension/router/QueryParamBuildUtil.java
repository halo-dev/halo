package run.halo.app.extension.router;

import static org.springdoc.core.fn.builders.arrayschema.Builder.arraySchemaBuilder;
import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;
import static org.springdoc.core.fn.builders.schema.Builder.schemaBuilder;

import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.annotations.enums.Explode;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.enums.ParameterStyle;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Schema;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.springdoc.core.fn.builders.operation.Builder;

public final class QueryParamBuildUtil {

    private QueryParamBuildUtil() {
    }

    private static <T> T defaultIfNull(T t, T defaultValue) {
        return t == null ? defaultValue : t;
    }

    private static <T> List<T> defaultIfNull(List<T> list, List<T> defaultValue) {
        return list == null ? defaultValue : list;
    }

    private static String toStringOrNull(Object obj) {
        return obj == null ? null : obj.toString();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void buildParametersFromType(Builder operationBuilder, Type queryParamType) {
        var resolvedSchema =
            ModelConverters.getInstance().readAllAsResolvedSchema(queryParamType);
        var properties = (Map<String, Schema>) resolvedSchema.schema.getProperties();
        var requiredNames = defaultIfNull(resolvedSchema.schema.getRequired(),
            Collections.emptyList());
        properties.forEach((propName, propSchema) -> {
            final var paramBuilder = parameterBuilder().in(ParameterIn.QUERY);
            paramBuilder.name(propSchema.getName())
                .description(propSchema.getDescription())
                .style(ParameterStyle.FORM)
                .explode(Explode.TRUE);
            if (requiredNames.contains(propSchema.getName())) {
                paramBuilder.required(true);
            }

            if (propSchema instanceof ArraySchema arraySchema) {
                paramBuilder.array(arraySchemaBuilder()
                    .uniqueItems(defaultIfNull(arraySchema.getUniqueItems(), false))
                    .minItems(defaultIfNull(arraySchema.getMinItems(), 0))
                    .maxItems(defaultIfNull(arraySchema.getMaxItems(), Integer.MAX_VALUE))
                    .arraySchema(convertSchemaBuilder(arraySchema))
                    .schema(convertSchemaBuilder(arraySchema.getItems()))
                );
            } else {
                paramBuilder.schema(convertSchemaBuilder(propSchema));
            }
            operationBuilder.parameter(paramBuilder);
        });
    }

    private static org.springdoc.core.fn.builders.schema.Builder convertSchemaBuilder(
        Schema<?> schema) {
        var allowableValues = new String[0];
        if (schema.getEnum() != null) {
            allowableValues = schema.getEnum().stream()
                .map(Object::toString)
                .toArray(String[]::new);
        }
        return schemaBuilder()
            .name(schema.getName())
            .type(schema.getType())
            .description(schema.getDescription())
            .format(schema.getFormat())
            .deprecated(defaultIfNull(schema.getDeprecated(), false))
            .nullable(defaultIfNull(schema.getNullable(), false))
            .allowableValues(allowableValues)
            .defaultValue(toStringOrNull(schema.getDefault()))
            .example(toStringOrNull(schema.getExample()));
    }
}
