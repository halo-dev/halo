package run.halo.app.model.enums.converter;

import run.halo.app.model.enums.ValueEnum;
import run.halo.app.utils.ReflectionUtils;

import javax.persistence.AttributeConverter;
import java.lang.reflect.Type;
import java.util.Objects;

/**
 * Abstract converter.
 *
 * @param <E> enum generic
 * @param <V> value generic
 * @author johnniang
 * @date 12/6/18
 */
public abstract class AbstractConverter<E extends ValueEnum<V>, V> implements AttributeConverter<E, V> {

    private final Class<E> clazz;

    @SuppressWarnings("unchecked")
    protected AbstractConverter() {
        Type enumType = Objects.requireNonNull(
                ReflectionUtils.getParameterizedTypeBySuperClass(AbstractConverter.class, this.getClass())
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
