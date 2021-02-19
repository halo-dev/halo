package run.halo.app.utils;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import org.springframework.http.HttpMethod;
import org.springframework.util.Assert;
import run.halo.app.model.entity.User;
import run.halo.app.security.authentication.Authentication;
import run.halo.app.security.support.UserDetail;
import springfox.documentation.builders.AlternateTypeBuilder;
import springfox.documentation.builders.AlternateTypePropertyBuilder;
import springfox.documentation.builders.ResponseBuilder;
import springfox.documentation.service.Response;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

/**
 * Swagger utils.
 *
 * @author johnniang
 */
public final class SwaggerUtils {

    private SwaggerUtils() {
    }

    public static Type customMixin(Class<?> clazz,
        List<Consumer<AlternateTypePropertyBuilder>> properties) {
        Assert.notNull(clazz, "Class must not be null");
        final var typeBuilder = new AlternateTypeBuilder()
            .fullyQualifiedClassName(
                String.format("%s.generated.%s", clazz.getPackage().getName(),
                    clazz.getSimpleName()));

        properties.forEach(typeBuilder::property);

        return typeBuilder.build();
    }

    public static Type emptyMixin(Class<?> clazz) {
        return customMixin(clazz, Collections.emptyList());
    }

    public static Consumer<AlternateTypePropertyBuilder> propertyBuilder(Class<?> clazz,
        String name) {
        return propertyBuilder -> propertyBuilder.type(clazz)
            .name(name)
            .canRead(true)
            .canWrite(true);
    }

    public static final List<Response> GLOBAL_RESPONSES = Arrays.asList(
        new ResponseBuilder().code("200").description("The request has succeeded.").isDefault(true)
            .build(),
        new ResponseBuilder().code("201").description(
            "The request has succeeded and a new resource has been created as a result.").build(),
        new ResponseBuilder().code("204").description(
            "There is no content to send for this request, but the headers may be useful.").build(),
        new ResponseBuilder().code("400")
            .description("The server could not understand the request due to invalid syntax.")
            .build(),
        new ResponseBuilder().code("401").description("Although the HTTP standard specifies "
            + "\"unauthorized\", semantically this response means \"unauthenticated\"").build(),
        new ResponseBuilder().code("403")
            .description("The client does not have access rights to the content.").build(),
        new ResponseBuilder().code("404")
            .description("The server can not find the requested resource.").build(),
        new ResponseBuilder().code("405").description(
            "The request method is known by the server but has been disabled and cannot be used. ")
            .build(),
        new ResponseBuilder().code("500")
            .description("The server has encountered a situation it doesn't know how to handle.")
            .build(),
        new ResponseBuilder().code("501")
            .description("The request method is not supported by the server and cannot be handled.")
            .build(),
        new ResponseBuilder().code("503")
            .description("The server is not ready to handle the request.").build());

    public static Docket defaultDocket() {
        return new Docket(DocumentationType.OAS_30)
            .forCodeGeneration(true)
            .ignoredParameterTypes(initIgnore())
            .useDefaultResponseMessages(false)
            .globalResponses(HttpMethod.GET, GLOBAL_RESPONSES)
            .globalResponses(HttpMethod.POST, GLOBAL_RESPONSES)
            .globalResponses(HttpMethod.DELETE, GLOBAL_RESPONSES)
            .globalResponses(HttpMethod.PATCH, GLOBAL_RESPONSES)
            .globalResponses(HttpMethod.PUT, GLOBAL_RESPONSES);
    }

    public static Optional<Class<?>> classFor(String className) {
        try {
            return Optional
                .of(Class.forName(className, false, SwaggerUtils.class.getClassLoader()));
        } catch (ClassNotFoundException e) {
            return Optional.empty();
        }
    }

    private static Class<?>[] initIgnore() {
        final Set<Class<?>> ignoredClasses = new HashSet<>();
        ignoredClasses.add(User.class);
        ignoredClasses.add(UserDetail.class);
        ignoredClasses.add(Authentication.class);

        classFor(User.class.getName()).ifPresent(ignoredClasses::add);
        return ignoredClasses.toArray(Class[]::new);
    }

}
