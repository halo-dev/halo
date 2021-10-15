package run.halo.app.model.dto.base;

import static run.halo.app.utils.BeanUtils.updateProperties;

import org.springframework.lang.NonNull;

/**
 * Converter interface for output DTO.
 *
 * <b>The implementation type must be equal to DTO type</b>
 *
 * @param <DtoT> the implementation class type
 * @param <D> domain type
 * @author johnniang
 */
public interface OutputConverter<DtoT extends OutputConverter<DtoT, D>, D> {

    /**
     * Convert from domain.(shallow)
     *
     * @param domain domain data
     * @return converted dto data
     */
    @SuppressWarnings("unchecked")
    @NonNull
    default <T extends DtoT> T convertFrom(@NonNull D domain) {

        updateProperties(domain, this);

        return (T) this;
    }
}
