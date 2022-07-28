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
            var allowableValues = new String[0];
            if (propSchema.getEnum() != null) {
                allowableValues = (String[]) propSchema.getEnum().stream()
                    .map(Object::toString)
                    .toArray(String[]::new);
            }
            paramBuilder.schema(schemaBuilder()
                .type(propSchema.getType())
                .format(propSchema.getFormat())
                .nullable(defaultIfNull(propSchema.getNullable(), false))
                .allowableValues(allowableValues)
                .defaultValue(toStringOrNull(propSchema.getDefault()))
                .example(toStringOrNull(propSchema.getExample()))
            );

            if (propSchema instanceof ArraySchema arraySchema) {
                paramBuilder.array(arraySchemaBuilder()
                    .uniqueItems(defaultIfNull(arraySchema.getUniqueItems(), false))
                    .minItems(defaultIfNull(arraySchema.getMinItems(), 0))
                    .maxItems(defaultIfNull(arraySchema.getMaxItems(), Integer.MAX_VALUE))
                    .schema(schemaBuilder()
                        .type(arraySchema.getItems().getType())
                        .format(arraySchema.getItems().getFormat())
                        .defaultValue(toStringOrNull(arraySchema.getItems().getDefault())))
                );
            }
            operationBuilder.parameter(paramBuilder);
        });
    }
}
