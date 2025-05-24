package run.halo.app.extension.router;

import static org.springdoc.core.fn.builders.arrayschema.Builder.arraySchemaBuilder;
import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;
import static org.springdoc.core.fn.builders.schema.Builder.schemaBuilder;

import io.swagger.v3.oas.annotations.enums.ParameterIn;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

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

}
