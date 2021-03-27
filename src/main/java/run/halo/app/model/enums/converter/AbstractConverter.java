package run.halo.app.model.enums.converter;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.Objects;
import javax.persistence.AttributeConverter;
import run.halo.app.model.enums.ValueEnum;
import run.halo.app.utils.ReflectionUtils;

/**
 * Abstract converter.
 *
 * @param <E> enum generic
 * @param <V> value generic
 * @author johnniang
 * @date 12/6/18
 */
@Deprecated
public abstract class AbstractConverter<E extends Enum<E> & ValueEnum<V>, V extends Serializable>

    implements AttributeConverter<E, V> {

    private final Class<E> clazz;

    @SuppressWarnings("unchecked")
    protected AbstractConverter() {
        Type enumType = Objects.requireNonNull(ReflectionUtils
            .getParameterizedTypeBySuperClass(AbstractConverter.class, this.getClass())
        ).getActualTypeArguments()[0];
        this.clazz = (Class<E>) enumType;
    }

    @Override
    public V convertToDatabaseColumn(E attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public E convertToEntityAttribute(V dbData) {
        return dbData == null ? null : ValueEnum.valueToEnum(clazz, dbData);
    }
}
