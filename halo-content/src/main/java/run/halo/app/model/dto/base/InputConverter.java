package run.halo.app.model.dto.base;

import org.springframework.lang.Nullable;
import run.halo.app.utils.BeanUtils;
import run.halo.app.utils.ReflectionUtils;

import java.lang.reflect.ParameterizedType;
import java.util.Objects;

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
        ParameterizedType currentType = parameterizedType();

        // Assert not equal
        Objects.requireNonNull(currentType, "Cannot fetch actual type because parameterized type is null");

        Class<DOMAIN> domainClass = (Class<DOMAIN>) currentType.getActualTypeArguments()[0];

        return BeanUtils.transformFrom(this, domainClass);
    }

    /**
     * Update a domain by dto.(shallow)
     *
     * @param domain updated domain
     */
    default void update(DOMAIN domain) {
        BeanUtils.updateProperties(this, domain);
    }

    /**
     * Get parameterized type.
     *
     * @return parameterized type or null
     */
    @Nullable
    default ParameterizedType parameterizedType() {
        return ReflectionUtils.getParameterizedType(InputConverter.class, this.getClass());
    }
}

