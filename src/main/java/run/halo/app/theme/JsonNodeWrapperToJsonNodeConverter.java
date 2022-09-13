package run.halo.app.theme;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Collections;
import java.util.Set;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/**
 * The {@link Converter} implementation for the
 * conversion
 * of {@link JsonPropertyAccessor.JsonNodeWrapper} to {@link JsonNode},
 * when the {@link JsonPropertyAccessor.JsonNodeWrapper} can be a result of the expression
 * for JSON in case of the {@link JsonPropertyAccessor} usage.
  Reference from
 * <a href="https://github.com/spring-projects/spring-integration">spring-integration</a>
 *
 * @author guqing
 * @see <a href="https://github.com/spring-projects/spring-integration">spring-integration</a>
 * @since 2.0.0
 */
public class JsonNodeWrapperToJsonNodeConverter implements GenericConverter {

    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        return Collections.singleton(
            new ConvertiblePair(JsonPropertyAccessor.JsonNodeWrapper.class, JsonNode.class));
    }

    @Override
    @Nullable
    public Object convert(@Nullable Object source, @NonNull TypeDescriptor sourceType,
        @NonNull TypeDescriptor targetType) {
        if (source != null) {
            return targetType.getObjectType()
                .cast(((JsonPropertyAccessor.JsonNodeWrapper<?>) source).getRealNode());
        }
        return null;
    }
}
