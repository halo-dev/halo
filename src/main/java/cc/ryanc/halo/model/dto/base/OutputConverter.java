package cc.ryanc.halo.model.dto.base;

/**
 * Converter interface for output DTO.
 *
 * @author johnniang
 */
public interface OutputConverter<DTO, DOMAIN> {

    /**
     * Convert from domain.(shallow)
     *
     * @param domain domain data
     * @return converted dto data
     */
    DTO convertFrom(DOMAIN domain);
}
