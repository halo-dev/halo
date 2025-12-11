package run.halo.app.extension.index;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * String to UnknownKey converter.
 *
 * @author johnniang
 * @since 2.22.0
 * @deprecated for backward compatibility. May remove with {@link UnknownKey} in the future
 *
 */
@Component
@Deprecated(forRemoval = true, since = "2.22.0")
class StringUnknownKeyConverter implements Converter<String, UnknownKey> {

    @Override
    public UnknownKey convert(String source) {
        return new UnknownKey(source);
    }

}
