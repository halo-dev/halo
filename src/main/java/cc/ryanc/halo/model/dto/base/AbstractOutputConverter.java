package cc.ryanc.halo.model.dto.base;

import org.springframework.util.Assert;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import static cc.ryanc.halo.utils.BeanUtils.updateProperties;

/**
 * Abstract output dto converter. (it must be extended by DTO)
 *
 * @author johnniang
 */
@Deprecated
public abstract class AbstractOutputConverter<DTO extends OutputConverter<DTO, DOMAIN>, DOMAIN> implements OutputConverter<DTO, DOMAIN> {

    @SuppressWarnings("unchecked")
    private final Class<DTO> dtoType = (Class<DTO>) fetchType(0);

    public AbstractOutputConverter() {
        Assert.isTrue(dtoType.equals(getClass()), "this converter must be extended by DTO type");
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends DTO> T convertFrom(DOMAIN domain) {
        updateProperties(domain, this);
        return (T) this;
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
