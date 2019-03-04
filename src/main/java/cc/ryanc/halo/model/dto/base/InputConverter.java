package cc.ryanc.halo.model.dto.base;

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
    DOMAIN convertTo();

    /**
     * Update a domain by dto.(shallow)
     *
     * @param domain updated domain
     */
    void update(DOMAIN domain);
}

