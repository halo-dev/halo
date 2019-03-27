package cc.ryanc.halo.model.enums.converter;

import cc.ryanc.halo.model.enums.ValueEnum;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.AttributeConverter;

/**
 * Abstract converter.
 *
 * @param <E> enum generic
 * @param <V> value generic
 * @author johnniang
 * @date 12/6/18
 */
@Slf4j
public abstract class AbstractConverter<E extends ValueEnum<V>, V> implements AttributeConverter<E, V> {

    private final Class<E> clazz;

    protected AbstractConverter(Class<E> clazz) {
        this.clazz = clazz;
    }

    @Override
    public V convertToDatabaseColumn(E attribute) {
        log.debug("Convert to database column: [{}], class type: [{}]", attribute, clazz.getSimpleName());
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public E convertToEntityAttribute(V dbData) {
        log.debug("Convert to entity attribute: [{}], class type: [{}]", dbData, clazz.getSimpleName());
        return dbData == null ? null : ValueEnum.valueToEnum(clazz, dbData);
    }
}
