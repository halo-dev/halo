package cc.ryanc.halo.cache;

import lombok.*;

import java.util.Date;

/**
 * Cache wrapper.
 *
 * @author johnniang
 */
@Data
@EqualsAndHashCode
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CacheWrapper<V> {

    /**
     * Cache data
     */
    private V data;

    /**
     * Expired time.
     */
    private Date expireAt;

    /**
     * Create time.
     */
    private Date createAt;
}
