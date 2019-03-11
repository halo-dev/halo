package cc.ryanc.halo.model.dto.base;

import cc.ryanc.halo.utils.ReflectionUtils;

import java.lang.reflect.ParameterizedType;
import java.util.Objects;

import static cc.ryanc.halo.utils.BeanUtils.transformFrom;
import static cc.ryanc.halo.utils.BeanUtils.updateProperties;

/**
 * Converter interface for input DTO.
 *
 * @author johnniang
 */
public interface InputConverter<DOMAIN> {

    /**
     * Convert to domain.(shallow)
     *
     * @return new domain with same value(not null)
     */
    @SuppressWarnings("unchecked")
    default DOMAIN convertTo() {
        // Get parameterized type
        ParameterizedType currentType = ReflectionUtils.getParameterizedType(InputConverter.class, this.getClass());

        // Assert not equal
        Objects.requireNonNull(currentType, "Cannot fetch actual type because parameterized type is null");

        Class<DOMAIN> domainClass = (Class<DOMAIN>) currentType.getActualTypeArguments()[0];

        return transformFrom(this, domainClass);
    }

    /**
     * Update a domain by dto.(shallow)
     *
     * @param domain updated domain
     */
    default void update(DOMAIN domain) {
        updateProperties(this, domain);
    }
}

