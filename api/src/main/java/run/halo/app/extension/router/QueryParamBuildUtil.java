package run.halo.app.extension.router;

import static org.springdoc.core.fn.builders.arrayschema.Builder.arraySchemaBuilder;
import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;
import static org.springdoc.core.fn.builders.schema.Builder.schemaBuilder;

import io.swagger.v3.oas.annotations.enums.ParameterIn;
import java.lang.reflect.Type;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.fn.builders.operation.Builder;

@Slf4j
@UtilityClass
public class QueryParamBuildUtil {

    public static org.springdoc.core.fn.builders.parameter.Builder sortParameter() {
        return parameterBuilder()
            .in(ParameterIn.QUERY)
            .name("sort")
            .required(false)
            .description("""
                Sorting criteria in the format: property,(asc|desc). \
                Default sort order is ascending. Multiple sort criteria are supported.\
                """)
            .array(arraySchemaBuilder().schema(schemaBuilder().type("string")));
    }

    @Deprecated(since = "2.15.0")
    public static void buildParametersFromType(Builder operationBuilder, Type queryParamType) {
        log.warn(
            "Deprecated method QueryParamBuildUtil.buildParametersFromType is called, please use "
                + "'org.springdoc.core.fn.builders.operation.Builder#parameter' method instead."
                + "This method will be removed in Halo 2.20.0 version.");
    }
}
