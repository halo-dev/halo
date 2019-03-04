package cc.ryanc.halo.model.dto.base;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import static cc.ryanc.halo.utils.BeanUtils.transformFrom;
import static cc.ryanc.halo.utils.BeanUtils.updateProperties;

/**
 * Convenience for input dto.Abstract input dto converter.
 *
 * @author johnniang
 */
public abstract class AbstractInputConverter<DOMAIN> implements InputConverter<DOMAIN> {

    @SuppressWarnings("unchecked")
    private final Class<DOMAIN> domainType = (Class<DOMAIN>) fetchType(0);

    @Override
    public DOMAIN convertTo() {
        return transformFrom(this, domainType);
    }

    @Override
    public void update(DOMAIN domain) {
        updateProperties(this, domain);
    }

    /**
     * Get actual generic type.
     *
     * @param index generic type index
     * @return real type will be returned
     */
    private Type fetchType(int index) {
        return ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[index];
    }

}
